# Reports

- **Status:** Shipped
- **Owner:** TBD
- **Related specs:** [[backlog-sprints]]
- **Last updated:** 2026-07-09

> Shipped: `GET /api/v1/projects/{key}/reports` returns headline totals (issues, done, points,
> done points), issue distributions by status/priority/type (every enum bucket present incl.
> zeros, in enum order), and per-sprint velocity (completed vs total points). UI: stat tiles,
> three bar-distribution charts, and a velocity chart (all CSS bars, no chart lib). Verified
> live. Deferred: burndown-over-time and cumulative-flow (need per-day historical snapshots).

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
