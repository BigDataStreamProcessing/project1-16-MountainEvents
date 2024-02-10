# Charakterystyka danych

Dane dotyczą końcowych efektów wypraw górskich. 

W strumieniu pojawiają się zdarzenia zgodne ze schematem `MountainEvent`.

```
create json schema MountainEvent(peak_name string, trip_leader string, result string, amount_people int, ets string, its string);
```

Każde zdarzenie związane jest z zakończeniem wyprawy wysokogórskiej. 
Informacja zawarta w zdarzeniu składa się z nazwy szczytu, lidera wyprawy, rezultatu wyprawy, liczby osób, która brała udział w wyprawie.

Dane uzupełnione są o dwie etykiety czasowe. 
* Pierwsza (`ets`) związana jest z momentem zakończenia wyprawy. Etykieta ta może się losowo spóźniać w stosunku do czasu systemowego maksymalnie do 42 sekund.
* Druga (`its`) związana jest z momentem rejestracji zdarzenia zakończenia wyprawy w systemie.

# Opis atrybutów

Atrybuty w każdym zdarzeniu zgodnym ze schematem `MountainEvent` mają następujące znaczenie:

* `peak_name` - nazwa szczytu
* `trip_leader` - imię i nazwisko lidera wyprawy (znany alpinista/alpinistka)
* `result` - opis rezultatu wyprawy (summit reached, base reached, resignation injury, resignation weather, resignation someone    missing, resignation other)
* `amount_people` - liczba osób biorąca udział w wyprawie
* `ets` - moment zakończenia wyprawy
* `its` - czas rejestracji faktu zakończenia wyprawy

# Zadania
Opracuj rozwiązania poniższych zadań. 
* Opieraj się strumieniu zdarzeń zgodnych ze schematem `MountainEvent`
* W każdym rozwiązaniu możesz skorzystać z jednego lub kilku poleceń EPL.
* Ostatnie polecenie będące ostatecznym rozwiązaniem zadania musi 
  * być poleceniem `select` 
  * posiadającym etykietę `answer`, przykładowo:

```sql
@name('answer') 
SELECT peak_name, trip_leader, result, amount_people, ets, its 
FROM MountainEvent#ext_timed(java.sql.Timestamp.valueOf(its).getTime(), 3 sec);
```

## Zadanie 1
Dla każdego rezultatu wyprawy utrzymuj liczbę osób, jakiej udało się wejść na szczyt lub zdobyć bazę dla wypraw zarejestrowanych w ciągu ostatnich 10 sekund.

Wyniki powinny zawierać, następujące kolumny:
- `result` - rezultat wyprawy
- `sum_people` - suma osób, którym udało się wejść na szczyt lub zdobyć bazę w ciągu ostatnich 10 sekund

## Zadanie 2
Wykrywaj przypadki końca wypraw z powodu zaginięcia (resignation someone missing), ogranicz analizę tylko do wypraw, w których brało udział mniej niż 3 osoby.

Wyniki powinny zawierać, następujące kolumny:
- `peak_name` - nazwa szczytu
- `trip_leader` - imię i nazwisko lidera wyprawy
- `result` - rezultat wyprawy
- `amount_people` - liczba osób biorąca udział w wyprawie

## Zadanie 3
Analizując jedynie wyprawy zakończone z powodu zaginięcia (resignation someone missing), wykrywaj takie, w których liczba osób jest największa z ostatnich 20. wypraw.

Wyniki powinny zawierać, następujące kolumny:
- `peak_name` - nazwa szczytu
- `trip_leader` - imię i nazwisko lidera wyprawy
- `result` - rezultat wyprawy
- `amount_people` - liczba osób biorąca udział w wyprawie
- `max_people` - maksymalna liczba osób w ostatnich 20 wyprawach
- `its` - czas rejestracji faktu zakończenia wyprawy

## Zadanie 4
Dla każdych kolejnych 5 sekund wyznaczana jest lista z nazwami 10 szczytów, które były celem największej liczby wypraw. Znajduj nazwy takich szczytów, które zostały usunięte z powyższej listy. 

Wyniki powinny zawierać, następujące kolumny:
- `peak_name` - nazwa szczytu
- `how_many` - liczba wypraw, jaka miała na celu szczyt przed jego usunięciem z listy

## Zadanie 5
Wykrywaj serię nie dłuższą niż 5 sekund i składającą się z co najmniej 3 kolejnych wypraw zakończonych sukcesem (summit reached, base reached), po której to serii miała miejsce wyprawa o efekcie innym niż sukces.

Wyniki powinny zawierać, następujące kolumny:
- `result_1` - rezultat pierwszej wyprawy w serii
- `result_2` - rezultat drugiej wyprawy w serii
- `result_3` - rezultat trzeciej wyprawy w serii
- `its_1` - czas rejestracji faktu zakończenia pierwszej wyprawy w serii
- `its_2` - czas rejestracji faktu zakończenia drugiej wyprawy w serii
- `its_3` - czas rejestracji faktu zakończenia trzeciej wyprawy w serii


## Zadanie 6
Wykrywaj przypadki liderów wypraw, których kolejne wyprawy zakończyły się trzykrotnie niepowodzeniem lub trzykrotnie powodzeniem.
Zadbaj o to, aby wykluczyć przypadki, w których pomiędzy pierwszą wyprawą a trzecią nastąpiły wyprawy innego lidera.

Wyniki powinny zawierać, następujące kolumny:
- `trip_leader` - imię i nazwisko lidera wypraw
- `result_1` - rezultat pierwszej wyprawy
- `result_2` - rezultat drugiej wyprawy
- `result_3` - rezultat trzeciej wyprawy 

## Zadanie 7
Dla każdego lidera wykrywaj takie wyprawy, że:
  a) pierwsza wyprawa się nie udała
  b) od 3 do 5 kolejnych wypraw zakończyło się sukcesem (summit reached, base reached) i w każdej z nich brało udział mniej osób niż w pierwszej wyprawie
  c) ostatnia wyprawa zakończyła się porażką i brało w niej udział tyle samo lub więcej osób co w pierwszej wyprawie

Wyniki powinny zawierać, następujące kolumny:
- `its_a` - czas rejestracji faktu zakończenia wyprawy a
- `trip_leader` - imię i nazwisko lidera wypraw
- `result_a` - rezultat wyprawy a
- `result_b_first` - rezultat pierwszej wyprawy b
- `result_c` - rezultat wyprawy c
- `amount_people_a` - liczba osób w wyprawie a
- `amount_people_b_first` - liczba osób w pierwszej wyprawie b
- `amount_people_c` - liczba osób w wyprawie c
