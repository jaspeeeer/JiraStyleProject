# Timeline

- **Status:** Shipped
- **Owner:** TBD
- **Related specs:** [[roadmap]], [[backlog-sprints]]
- **Last updated:** 2026-07-09

> Shipped: `GET /api/v1/projects/{key}/timeline` returns the overall date range, dated sprint
> bands, and one bar per issue (start = created date in UTC; end = due date, clamped to ≥ start,
> or start again when there's no due date). UI: month-axis Gantt with sprint bands + status-
> colored issue bars linking to the issue detail; empty state when nothing is dated. Verified
> live. Deferred: dependency arrows, drag-to-reschedule, zoom.

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
