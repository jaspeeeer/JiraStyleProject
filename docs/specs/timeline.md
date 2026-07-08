# Timeline

- **Status:** Draft — deferred (Phase 6 scaffold shipped)
- **Owner:** TBD
- **Related specs:** [[roadmap]], [[backlog-sprints]]
- **Last updated:** 2026-07-08

> Scaffold in place: reachable `/timeline` page (empty state) wired to the stub
> `GET /api/v1/projects/{key}/timeline` (returns `status: SCAFFOLD`). Backend feature package
> `timeline` (controller/service/impl/dto) is ready to flesh out. Gantt view deferred.

## 1. Purpose & scope

Gantt-style timeline of issues/epics with dependencies. **Phase 6 scaffold only:** route +
empty-state UI + stub endpoint. Full timeline deferred until prioritized.

## 2. User stories

- As a project lead, I want a timeline so that I can see scheduling and overlap. *(deferred)*

## 3. Data model

Reads `Issue` (due dates), `Epic`, `Sprint`; no new tables for the scaffold.

## 4. API contract

| Method | Path | Auth | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/projects/{key}/timeline` | JWT | — | stub placeholder | — |

## 5. UI / behavior

`(app)/timeline`. Empty-state page reachable from the sidebar.

## 6. Business rules & validation

TBD when prioritized.

## 7. Acceptance criteria

- [ ] Route loads with an empty state; sidebar link works.

## 8. Test cases

- **Frontend:** Playwright smoke — page loads.

## 9. Open questions

- Overlap of responsibilities with [[roadmap]]?
