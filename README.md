# InTrack

A simple, Jira-style internal issue tracker. Monorepo:

- **`backend/`** — Spring Boot 3.3 (Java 21) + Maven + PostgreSQL + Flyway
- **`frontend/`** — Next.js 15 (App Router) + TypeScript + Tailwind + npm

---

## 1. Prerequisites

Install these once:

| Tool | Version | Notes |
| --- | --- | --- |
| **Java (JDK)** | 21 (LTS) | e.g. [Eclipse Temurin 21](https://adoptium.net/). Ensure `JAVA_HOME` points at it. |
| **Node.js** | 20+ (24 works) | Ships with **npm** (v11 tested). |
| **PostgreSQL** | 16 or 17 | Either a local install **or** via Docker (see step 2). |
| **Maven** | 3.9.x | Optional if you use the committed wrapper — see [step 3](#3-run-the-backend-8080). |
| **Docker** | latest | Optional — only if you run Postgres via `docker compose`. |
| **Task** | latest | Optional — [taskfile.dev](https://taskfile.dev) for the `task` shortcuts. |

Check what you have:

```bash
java -version      # → 21.x
node -v            # → v20+  (v24 ok)
npm -v             # → 11.x
```

---

## 2. Start PostgreSQL

The backend expects a database `intrack` owned by role `intrack` (password `intrack`) on
`localhost:5432` — this is configured in
[`backend/src/main/resources/application-local.yml`](backend/src/main/resources/application-local.yml).

Pick **one** of the following.

### Option A — Docker (recommended, zero setup)

```bash
docker compose up -d          # starts Postgres (:5432) + Mailhog (:1025/:8025)
```

The `docker-compose.yml` already creates the `intrack` database, user, and password.

### Option B — Existing local PostgreSQL

If you already run Postgres locally, create the role and database once (using a superuser such
as `postgres`):

```sql
CREATE ROLE intrack LOGIN PASSWORD 'intrack';
CREATE DATABASE intrack OWNER intrack;
```

Or in one line with `psql`:

```bash
psql -U postgres -h localhost -c "CREATE ROLE intrack LOGIN PASSWORD 'intrack';"
psql -U postgres -h localhost -c "CREATE DATABASE intrack OWNER intrack;"
```

> Flyway creates all tables automatically on first backend startup — you do **not** run any SQL
> by hand beyond creating the empty database.

---

## 3. Run the backend (:8080)

From `backend/`:

```bash
cd backend
./mvnw spring-boot:run      # or: mvn spring-boot:run  (if Maven is installed globally)
```

> **Maven wrapper note:** the repo commits `.mvn/wrapper/maven-wrapper.properties` but not the
> `mvnw` scripts. Generate them once with a system Maven — `mvn -N wrapper:wrapper` — then
> `./mvnw` works with no global install. If you already have `mvn`, just use `mvn` directly.

The app runs under the **`local`** profile by default, which:

- connects to `localhost:5432/intrack`,
- runs Flyway migrations (`V1`–`V3`), and
- **seeds sample data** (two projects + two users) on an empty database.

When it's up you'll see `Started IntrackApplication` in the logs.

### Verify

```bash
curl http://localhost:8080/api/v1/health      # → {"status":"UP"}
```

---

## 4. Run the frontend (:3000)

From `frontend/`:

```bash
cd frontend
npm install       # first time only
npm run dev       # → http://localhost:3000
```

Open **http://localhost:3000** — you'll be redirected to the login page.

---

## 5. Sample login credentials

The backend seeds these accounts on first run (empty DB), all with the password **`password`**:

| Email | Password | Role |
| --- | --- | --- |
| `admin@intrack.local` | `password` | ADMIN (can access Users/Roles admin) |
| `dev@intrack.local` | `password` | DEVELOPER |

You can also **Create Account** from the login screen to register a new user (defaults to the
DEVELOPER role, status ACTIVE).

Seeded projects: **PROJ** (Platform) and **MOB** (Mobile App).

---

## 6. One-command dev (optional, needs Task)

With [Task](https://taskfile.dev) installed you can skip the per-app commands:

```bash
task up      # docker compose up -d  (Postgres + Mailhog)
task dev     # run backend (:8080) and frontend (:3000) together
task test    # backend `mvn verify` + frontend Playwright
task lint    # lint both apps
task down    # stop docker services
```

---

## 7. Running the tests

```bash
# Backend  (from backend/)
./mvnw -DskipITs test     # fast unit tests only (no Docker needed)
./mvnw verify             # unit + integration tests (needs Docker for Testcontainers)

# Frontend (from frontend/)
npm run lint              # eslint
npm run typecheck         # tsc --noEmit
npm run test:e2e          # Playwright (installs a browser on first run:
                          #   npx playwright install chromium)
```

---

## 8. Troubleshooting

**Backend fails to start with `Unable to establish loopback connection` (Windows + JDK 21).**
Tomcat's NIO selector creates an internal AF_UNIX socket under `%TEMP%`; some Windows setups
block that specific socket path. Point the JVM at a plain local directory:

```bash
# one-off
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=C:\jtmp"

# or set globally for the shell (create C:\jtmp first)
setx JAVA_TOOL_OPTIONS "-Djdk.net.unixdomain.tmpdir=C:\jtmp"
```

**`password authentication failed for user "intrack"`** — the role/database from step 2 doesn't
exist or has a different password. Re-run the `CREATE ROLE`/`CREATE DATABASE` statements, or use
the Docker option.

**Port already in use (8080 / 3000 / 5432)** — stop the process using the port, or change the
port (`server.port` for the backend, `npm run dev -- -p 3001` for the frontend).

**CORS errors in the browser console** — the backend only allows the origin
`http://localhost:3000` (see `intrack.app.frontend-base-url`). Run the frontend on that origin,
or update the property.

---

## 9. Where things are

- **Conventions & architecture:** [`CLAUDE.md`](./CLAUDE.md) — read this first.
- **Feature specs:** [`docs/specs/`](./docs/specs) — one file per feature, the source of truth.
- **Backend details:** [`backend/README.md`](./backend/README.md).
