# Roadmap

- **Status:** Shipped
- **Owner:** TBD
- **Related specs:** [[backlog-sprints]], [[timeline]]
- **Last updated:** 2026-07-09

> Shipped: **Epic CRUD** (`epic` feature — `GET /api/v1/projects/{key}/epics`,
> `POST /api/v1/epics` ADMIN/PROJECT_LEAD; epics gained `startDate`/`endDate` via `V4`) plus a
> real roadmap view. `GET /api/v1/projects/{key}/roadmap` returns each epic with timeframe +
> issue progress (`totalIssues`/`doneIssues`). UI: month-axis Gantt of scheduled epics (bars in
> the epic color, done-fraction overlay), undated epics grouped under "Unscheduled", and a
> New Epic modal. Verified live end-to-end. Deferred: epic edit/delete, drag-to-reschedule.

## 1. Purpose & scope

High-level view of epics/initiatives over time. **Phase 6 scaffold only:** route + empty-state
UI + stub endpoint. Full visualization deferred until prioritized.

## 2. User stories

- As a project lead, I want a roadmap view so that I can see epics across a timeframe. *(deferred)*

## 3. Data model

Reads `Epic` and `Issue`; no new tables anticipated for the scaffold.

## 4. API contract

| Method | Path | Auth | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/projects/{key}/roadmap` | JWT | — | stub placeholder | — |

## 5. UI / behavior

`(app)/roadmap`. Empty-state page reachable from the sidebar.

## 6. Business rules & validation

TBD when prioritized.

## 7. Acceptance criteria

- [ ] Route loads with an empty state; sidebar link works.

## 8. Test cases

- **Frontend:** Playwright smoke — page loads, no dead link.

## 9. Open questions

- Timeframe granularity (weeks/quarters)? Overlap with [[timeline]]?
