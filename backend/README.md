# InTrack backend

Spring Boot (Java 21) + PostgreSQL + Flyway. Layered: controller → service → repository.
Conventions live in the root [`CLAUDE.md`](../CLAUDE.md).

## First-time setup: Maven wrapper

This repo commits `.mvn/wrapper/maven-wrapper.properties` but not the wrapper scripts. Generate
them once (requires a system Maven available on your machine):

```bash
cd backend
mvn -N wrapper:wrapper     # creates mvnw, mvnw.cmd
```

After that, always use `./mvnw` (no global Maven needed). If you already have Maven installed you
can simply use `mvn` in place of `./mvnw`.

## Run

```bash
docker compose up -d              # from repo root — starts Postgres
./mvnw spring-boot:run            # profile: local (see application-local.yml)
# → http://localhost:8080/api/v1/health  ->  {"status":"UP"}
```

## Test

```bash
./mvnw -DskipITs test             # unit tests only (no Docker needed)
./mvnw verify                     # unit + integration (*IT) — needs Docker for Testcontainers
```

## Layout

```
com.standardinsurance.intrack
├── IntrackApplication
├── config/          # Spring configuration (security, cors, etc. — added per phase)
├── common/
│   ├── error/       # ErrorCode, ApiException, ErrorResponse, GlobalExceptionHandler
│   └── web/         # PageResponse<T>
├── system/          # health endpoint
└── <feature>/       # controller, service(+impl), repository, entity, mapper, dto/
```
