# InTrack

A simple, Jira-style internal issue tracker for Standard Insurance. This file is the
**single source of truth for how we build InTrack**. Read it once and you can contribute
consistently. Every rule here is "the rule every developer on this repo follows" —
consistency and predictability matter more than cleverness.

> Feature behavior lives in `docs/specs/<feature>.md`. This file describes the *system and
> its conventions*; specs describe *what each feature does*. A code change updates its spec
> in the same PR.

---

## 1. Project overview

InTrack is an internal-use issue tracker: boards, backlogs/sprints, issues with subtasks and
comments, and user/role administration. It is used by internal teams only; there is no public
signup. Guiding principle: **standardized, predictable code over cleverness.** When in doubt,
match the existing pattern.

## 2. Tech stack & versions

| Area | Choice | Version |
| --- | --- | --- |
| Backend | Spring Boot | 3.3.x (Java 21 LTS) |
| Backend build | **Maven** (via `./mvnw` wrapper) | 3.9.x |
| Persistence | Spring Data JPA / Hibernate + PostgreSQL | Postgres 16 |
| Migrations | Flyway | bundled |
| Mapping | MapStruct | 1.6.x |
| Backend tests | JUnit 5, Mockito, Spring Boot Test, Testcontainers | — |
| Frontend | Next.js (App Router) + TypeScript | Next 15, TS 5 |
| Frontend pkg mgr | **npm** (see note) | npm 11 |
| Styling | Tailwind CSS | v3 |
| Frontend tests | Playwright (E2E + component) | latest |
| Node | Node LTS | 20+ (24 works) |

> **Package manager note:** the plan originally proposed pnpm; we standardized on **npm**
> because it is the confirmed installed toolchain. Use `npm`, commit `package-lock.json`.

## 3. Monorepo layout

```
intrack/
├── CLAUDE.md              # This file — system spec + conventions + onboarding
├── README.md             # Human quickstart
├── Taskfile.yml          # Unified commands across both apps (see §4)
├── docker-compose.yml    # Local Postgres + Mailhog
├── .editorconfig         # Shared whitespace/encoding
├── .github/workflows/    # CI: backend (mvn verify) + frontend (lint/typecheck/e2e)
├── docs/specs/           # One markdown spec per feature — single source of truth
├── backend/              # Spring Boot app (Maven)
└── frontend/             # Next.js App Router app (npm)
```

Backend is packaged **feature-first**: each feature (`user/`, `issue/`, …) holds its full
vertical slice, and the *layer* is expressed by the class name (`UserController`,
`UserService`). See §5–§6.

## 4. Run / build / test

Prereqs: **Java 21**, **Node 20+**, **Docker** (for Postgres + integration tests).

From the repo root (requires [Task](https://taskfile.dev); or run the underlying commands directly):

```bash
task up          # start Postgres + Mailhog (docker compose up -d)
task dev         # run backend (:8080) and frontend (:3000) together
task test        # backend `mvn verify` + frontend Playwright
task lint        # backend + frontend linting
task down        # stop docker services
```

Per app (no Task needed):

```bash
# Backend  (from backend/)
./mvnw spring-boot:run        # run app on :8080  (profile: local)
./mvnw verify                 # unit + integration tests (needs Docker for Testcontainers)
./mvnw -q -DskipITs test      # unit tests only

# Frontend (from frontend/)
npm install
npm run dev                   # http://localhost:3000
npm run build                 # production build
npm run lint                  # eslint
npm run typecheck             # tsc --noEmit
npm run test:e2e              # playwright
```

## 5. Backend architecture

Strict layering — dependencies point **downward only**:

```
Controller  ->  Service (interface + Impl)  ->  Repository  ->  DB
   (DTOs)          (business logic, @Transactional)   (entities)
```

Rules (non-negotiable):

- **Controllers** are thin: HTTP mapping, validation (`@Valid`), delegate to a service, return
  DTOs. No business logic, no repository access.
- **Services** hold all business logic and own transactions (`@Transactional`). They accept and
  return DTOs at their public boundary and do entity↔DTO mapping via a `*Mapper`.
- **Repositories** are Spring Data interfaces. No business logic.
- **Entities never leave the service layer.** Controllers must never see or return an entity.

## 6. Backend naming conventions

| Type | Suffix | Example |
| --- | --- | --- |
| REST controller | `Controller` | `IssueController` |
| Service interface | `Service` | `IssueService` |
| Service implementation | `ServiceImpl` | `IssueServiceImpl` |
| Spring Data repository | `Repository` | `IssueRepository` |
| JPA entity | `Entity` | `IssueEntity` |
| MapStruct mapper | `Mapper` | `IssueMapper` |
| Request DTO | `RequestDto` | `CreateIssueRequestDto` |
| Response DTO | `ResponseDto` | `IssueResponseDto` |

- Base package: `com.standardinsurance.intrack`.
- One package per feature: `com.standardinsurance.intrack.<feature>`; DTOs under `<feature>.dto`.
- Cross-cutting code lives in `common/` (`common.error`, `common.web`) and `config/`.

## 7. DTO & mapping rules

- **Separate request and response DTOs.** Never reuse an entity as a request/response body.
- DTOs are Java `record`s where possible; validation annotations (`@NotNull`, `@Email`, …) go
  on request DTOs.
- Mapping is done by a **MapStruct** `@Mapper(componentModel = "spring")` interface per feature.
- Mapping is invoked **in the service layer**, never in the controller.

### Entities & Lombok

- Entities extend `common.domain.BaseEntity` (id + audited `createdAt`/`updatedAt`).
- Entities use Lombok **`@Getter @Setter @NoArgsConstructor`** only. **Never `@Data`/`@EqualsAndHashCode`/`@ToString`**
  on an entity (they recurse across JPA associations). DTOs are records and use no Lombok.
- Enum fields are persisted `@Enumerated(EnumType.STRING)`.
- Auditing is enabled by `config.JpaConfig` (`@EnableJpaAuditing`).

## 8. API conventions

- Base path: **`/api/v1`**. Plural, lowercase, hyphenated resource nouns.
- Pagination/sort: `?page=&size=&sort=field,dir`. Paged results use `PageResponse<T>`.
- Filtering: explicit query params (e.g. `?role=ADMIN&status=ACTIVE&q=jane`).
- Success responses return the resource DTO (or `PageResponse<T>`) directly.
- Errors return the standard `ErrorResponse` envelope (§9).

## 9. Error handling

- A single `@RestControllerAdvice` `GlobalExceptionHandler` translates exceptions.
- Domain errors throw a typed `ApiException` carrying an `ErrorCode` (enum) + HTTP status.
- Bean-validation failures (`@Valid`) map to **400** with field details.
- Every error body is:

```json
{
  "timestamp": "2026-07-08T10:00:00Z",
  "status": 404,
  "code": "ISSUE_NOT_FOUND",
  "message": "Issue PROJ-205 was not found",
  "details": [],
  "path": "/api/v1/issues/PROJ-205"
}
```

## 10. Database & migrations

- **Flyway only.** Schema changes are versioned SQL in `backend/src/main/resources/db/migration`
  named `V<n>__<description>.sql`. Never edit an applied migration — add a new one.
- `spring.jpa.hibernate.ddl-auto=validate`. Hibernate never creates/alters schema.
- Tables/columns are `snake_case`; entity fields are `camelCase`.

## 11. Frontend architecture

- **Next.js App Router.** Route groups: `(auth)` for unauthenticated pages (no shell),
  `(app)` for the authenticated sidebar shell.
- **Server Components by default**; add `"use client"` only for interactive components.
- Data access goes through typed modules in `src/lib/api/` — components never call `fetch`
  directly with raw URLs.
- Feature-scoped components/hooks/types live under `src/features/<feature>/`; shared primitives
  under `src/components/ui/`.

## 12. Frontend conventions

- Components are `PascalCase.tsx`; hooks are `useX.ts`; route files follow Next.js (`page.tsx`,
  `layout.tsx`).
- **No hard-coded colors.** Use Tailwind design tokens (see `tailwind.config.ts`): dark sidebar
  surface + **gold accent** for primary actions.
- Keep client state minimal; prefer server components + server data. Introduce a client store only
  when a spec requires it.

## 13. Testing conventions

**"Done" means tested.** Every phase/feature ships with its tests.

- **Backend**
  - `*Test.java` — fast unit tests, no Spring context, Mockito for collaborators.
  - `*IT.java` — integration tests via `@SpringBootTest` + Testcontainers Postgres
    (extend `AbstractIntegrationTest`). Run in the Maven `verify` (failsafe) phase.
  - Tests live beside the feature package under `src/test/java`.
- **Frontend**
  - Playwright specs in `frontend/e2e/<feature>.spec.ts` (one file per feature).
  - Component tests colocated where used.

## 14. Git conventions

- Trunk-based; short-lived branches named `type/short-desc` (e.g. `feat/board-drag-drop`,
  `fix/login-401`). Types: `feat|fix|chore|docs|test|refactor`.
- **Conventional Commits**: `feat(board): persist card status on drag`.
- PRs require: green CI, the feature's spec updated, and one review. **Squash-merge.**

## 15. Definition of Done

A change is done only when all are true:

1. Behavior matches (and the PR updated) `docs/specs/<feature>.md`.
2. Backend unit + integration tests and/or Playwright tests are written and green.
3. `lint` and `typecheck` pass for touched apps.
4. No entity crosses the service boundary; naming conventions followed.

## 16. Spec workflow

- Every feature has `docs/specs/<feature>.md`, created from `docs/specs/_TEMPLATE.md`.
- The spec is the source of truth: purpose, data model, API contract, UI behavior, acceptance
  criteria, and test cases. Update it in the same PR as the code.

## Deferred decisions

- **Deployment target.** Local Docker Compose only for now. No Dockerfiles / image publishing /
  Kubernetes yet — revisit when we pick a target.
- **SSO.** Stubbed secondary auth path; real IdP integration deferred.
