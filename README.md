# connectshaper

ConnectSphere is a Spring Boot microservices backend scaffold for an Instagram-style social platform. This Day 1 baseline sets up the service boundaries, build system, infrastructure definitions, and starter APIs so implementation can continue service-by-service without reworking the repo layout.

## Services

- `gateway-service` routes traffic and validates JWTs.
- `user-service` handles registration, login, JWT issuance, and profile APIs.
- `post-service` manages posts, likes, and comments.
- `follow-service` manages follow relationships.
- `feed-service` assembles and caches the home feed with Redis.
- `notification-service` consumes Kafka events and stores notifications.

## Tech stack

- Java 17
- Spring Boot 3.5.13
- Spring Cloud 2025.0.1
- Spring Security with JWT
- PostgreSQL
- Redis
- Apache Kafka
- Maven
- Docker Compose
- JUnit 5, Mockito, Testcontainers

## Project layout

```text
connectsphere/
  user-service/
  post-service/
  follow-service/
  feed-service/
  notification-service/
  gateway-service/
  infra/
  docs/
  docker-compose.yml
  README.md
  .gitignore
```

## Default ports

- Gateway: `8080`
- User Service: `8081`
- Post Service: `8082`
- Follow Service: `8083`
- Feed Service: `8084`
- Notification Service: `8085`

## Kafka topics

- `user-registered`
- `post-created`
- `post-liked`
- `comment-created`
- `user-followed`

## Quick start

1. Align `java` and `javac` to the same Java 17 installation.
2. Install Maven and Docker Desktop if they are not already on `PATH`.
3. Start infrastructure with `docker compose up -d`.
4. Import the root Maven project into IntelliJ IDEA or VS Code.
5. Run each service from its main application class.

## Docs

- Architecture: [docs/architecture.md](C:\Users\olede\Documents\ConnectShaper\connectsphere\docs\architecture.md)
- Tooling status: [docs/tooling-status.md](C:\Users\olede\Documents\ConnectShaper\connectsphere\docs\tooling-status.md)
