# Tooling Status

These checks were run on April 22, 2026 in the local workspace.

## Available

- Git
- VS Code
- IntelliJ IDEA Community Edition
- Postman
- Java 17 JDK via `javac`

## Needs attention

- `java` currently resolves to Java 21 runtime while `JAVA_HOME` and `javac` point to Java 17.
- Maven was not available on `PATH`.
- Docker CLI was not available on `PATH`.
- Kafka CLI tools were not available on `PATH`.

## Recommended next actions

1. Move the Java 17 `bin` directory ahead of the Java 21 JRE on `PATH`.
2. Install Maven and confirm `mvn -version`.
3. Install Docker Desktop and confirm `docker --version`.
4. Install Kafka locally only if you want native CLI tools; Docker Compose is enough for the repo baseline.

