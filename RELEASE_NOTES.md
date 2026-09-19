# Release notes

## v1.0.0 - first documented teaching baseline

First complete release of **Greek Fire Response Coordination System**, a console application in Java for the CSC (Computer Science Center) project library. Level: Intermediate.

### What is included

- A runnable console application with a numbered menu, preloaded sample data (14 incidents, 10 stations, 38 resources) and input validation that never terminates the program on wrong input.
- The eleven required features: report an incident (location, severity, date and time, status), manage resources, assign resources, refuse invalid assignments, change status through a state machine, list active incidents, show available and deployed resources, search and filter with visible algorithms, statistics by severity, status and region, and resource utilisation.
- Object-oriented design that covers classes and objects, encapsulation, constructors, instance and static methods, overloading, one- and two-dimensional arrays, composition, inheritance, polymorphism, enums, validation and exception handling. All storage is in plain arrays (no `ArrayList`, `HashMap`, streams or databases).
- Automated tests: 70 JUnit 5 tests in 8 classes (valid and invalid assignment, capacity limits, status transitions, statistics, search and filter, domain model, sample data, console menu).
- Bilingual documentation (`README.md` in Greek, `README.en.md` in English), a recorded terminal session in `docs/examples/`, 15 review questions and 7 extension challenges (none of them implemented).

### Setup impact

- Requires JDK 17 or newer and Maven 3.6.3 or newer. No configuration, database or network access is needed at run time.
- Build and test with `mvn test`; package with `mvn package` and run `java -jar target/csc-java-fire-response-system-1.0.0.jar`.

### Known limitations

- Data is held in memory only and is reset when the program exits (by design; see extension challenge on saving and loading).
- Storage uses fixed-capacity arrays (60 incidents, 12 stations, and a fixed number of resources per station); the program reports a clear message when a limit is reached.

### Migration

None. This is the first release.
