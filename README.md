# InTrack

A simple, Jira-style internal issue tracker. Monorepo: **`backend/`** (Spring Boot + Maven) and
**`frontend/`** (Next.js + npm).

## Quickstart

Prereqs: **Java 21**, **Node 20+**, **Docker**.

```bash
# 1. Start Postgres + Mailhog
docker compose up -d

# 2. Backend  (http://localhost:8080)
cd backend && ./mvnw spring-boot:run

# 3. Frontend (http://localhost:3000)
cd frontend && npm install && npm run dev
```

With [Task](https://taskfile.dev) installed you can use the shortcuts: `task up`, `task dev`,
`task test`, `task down`.

## Where things are

- **Conventions & architecture:** [`CLAUDE.md`](./CLAUDE.md) — read this first.
- **Feature specs:** [`docs/specs/`](./docs/specs) — one file per feature, the source of truth.

## Health check

Once the backend is running: `GET http://localhost:8080/api/v1/health` → `{"status":"UP"}`.
# JiraStyleProject
