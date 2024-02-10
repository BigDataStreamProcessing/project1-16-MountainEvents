package com.example.bigdata;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import net.datafaker.Faker;
import net.datafaker.fileformats.Format;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EsperClient {
    public static void main(String[] args) throws InterruptedException {
        int noOfRecordsPerSec;
        int howLongInSec;
        if (args.length < 2) {
            noOfRecordsPerSec = 2;
            howLongInSec = 5;
        } else {
            noOfRecordsPerSec = Integer.parseInt(args[0]);
            howLongInSec = Integer.parseInt(args[1]);
        }

        Configuration config = new Configuration();
        EPCompiled epCompiled = getEPCompiled(config);

        // Connect to the EPRuntime server and deploy the statement
        EPRuntime runtime = EPRuntimeProvider.getRuntime("http://localhost:port", config);
        EPDeployment deployment;
        try {
            deployment = runtime.getDeploymentService().deploy(epCompiled);
        }
        catch (EPDeployException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        EPStatement resultStatement = runtime.getDeploymentService().getStatement(deployment.getDeploymentId(), "answer");

        resultStatement.addListener( (newData, oldData, stmt, runTime) -> {
            for (EventBean eventBean : newData) {
                System.out.printf("R: %s%n", eventBean.getUnderlying());
            }
        });

        Faker faker = new Faker();

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + (1000L * howLongInSec)) {
            for (int i = 0; i < noOfRecordsPerSec; i++) {
                generateMountainEventData(runtime, faker);
            }
            waitToEpoch();
        }
    }

    private static EPCompiled getEPCompiled(Configuration config) {
        CompilerArguments compilerArgs = new CompilerArguments(config);

        // Compile the EPL statement
        EPCompiler compiler = EPCompilerProvider.getCompiler();
        EPCompiled epCompiled;

        try {
            epCompiled = compiler.compile("""
            @public @buseventtype create json schema MountainEvent(peak_name string, trip_leader string, result string,
            amount_people int, ets string, its string);
            
            @name('answer')
            SELECT peak_name, trip_leader, result, amount_people, ets, its
            FROM MountainEvent#ext_timed(java.sql.Timestamp.valueOf(its).getTime(), 3 sec)
            """, compilerArgs);
        }

        catch (EPCompileException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }
        return epCompiled;
    }

    private static void generateMountainEventData(EPRuntime runtime, Faker faker) {
        Random random = new Random();
        ArrayList<String> possibleResults = new ArrayList<>() {
            {
                add("summit-reached");
                add("base-reached");
                add("resignation-injury");
                add("resignation-weather");
                add("resignation-someone-missing");
                add("resignation-other");
            }
        };

        String record;
        String name = faker.mountain().name();
        String mountaineer = faker.mountaineering().mountaineer();

        Timestamp timestamp = faker.date().past(42, TimeUnit.SECONDS);
        timestamp.setNanos(0);

        Timestamp timestampITS = Timestamp.valueOf(LocalDateTime.now().withNano(0));

        record = Format.toJson()
                .set("peak_name", () -> name)
                .set("trip_leader", () -> mountaineer)
                .set("result", () -> possibleResults.get(random.nextInt(possibleResults.size())))
                .set("amount_people", () -> random.nextInt(12) + 1)
                .set("ets", timestamp::toString)
                .set("its", timestampITS::toString)
                .build()
                .generate();
        runtime.getEventService().sendEventJson(record, "MountainEvent");
    }

    static void waitToEpoch() throws InterruptedException {
        long millis = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(millis) ;
        Instant instantTrunc = instant.truncatedTo( ChronoUnit.SECONDS ) ;
        long millis2 = instantTrunc.toEpochMilli() ;
        TimeUnit.MILLISECONDS.sleep(millis2+1000-millis);
    }
}

