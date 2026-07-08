# Reports

- **Status:** Draft — deferred (Phase 6 scaffold shipped)
- **Owner:** TBD
- **Related specs:** [[backlog-sprints]]
- **Last updated:** 2026-07-08

> Scaffold in place: reachable `/reports` page (empty state) wired to the stub
> `GET /api/v1/projects/{key}/reports` (returns `status: SCAFFOLD`). Backend feature package
> `reports` (controller/service/impl/dto) is ready to flesh out. Charts deferred.

## 1. Purpose & scope

Analytics: velocity, burndown, throughput. **Phase 6 scaffold only:** route + empty-state UI +
stub endpoint. Charts deferred until prioritized.

## 2. User stories

- As a project lead, I want burndown/velocity so that I can track delivery. *(deferred)*

## 3. Data model

Reads `Issue`, `Sprint`, `ActivityLog`; no new tables for the scaffold.

## 4. API contract

| Method | Path | Auth | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/projects/{key}/reports` | JWT | — | stub placeholder | — |

## 5. UI / behavior

`(app)/reports`. Empty-state page reachable from the sidebar.

## 6. Business rules & validation

TBD when prioritized.

## 7. Acceptance criteria

- [ ] Route loads with an empty state; sidebar link works.

## 8. Test cases

- **Frontend:** Playwright smoke — page loads.

## 9. Open questions

- Which reports first (burndown vs velocity vs cumulative flow)?
