# Backlog + Sprints

- **Status:** Draft
- **Owner:** TBD
- **Related specs:** [[board]], [[issue-detail]]
- **Last updated:** 2026-07-08

## 1. Purpose & scope

Backlog grouped by sprint (Active / Planned) plus an unscheduled backlog; sprint lifecycle
(create / start / complete); adding issues to sprints. **Out of scope:** capacity planning,
velocity charts (see [[reports]]).

## 2. User stories

- As a project lead, I want to create and start a sprint so that the team can begin work.
- As a user, I want to move backlog issues into a sprint so that they are scheduled.
- As a project lead, I want to complete a sprint so that remaining work returns to the backlog.

## 3. Data model

`Sprint` (name, goal, status, startDate, endDate) 1:N `Issue`. Enum:
`SprintStatus (PLANNED, ACTIVE, COMPLETED)`. Progress = done/total issues.
Schema created in Flyway `V2__init_core.sql` (table `sprints`); entity `SprintEntity`.

## 4. API contract

| Method | Path | Auth | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/projects/{key}/backlog` | JWT | — | `BacklogResponseDto` (sprints + unscheduled) | `PROJECT_NOT_FOUND` |
| POST | `/api/v1/sprints` | PROJECT_LEAD | `CreateSprintRequestDto` | `SprintResponseDto` | `VALIDATION` |
| POST | `/api/v1/sprints/{id}/start` | PROJECT_LEAD | — | `SprintResponseDto` | `SPRINT_ALREADY_ACTIVE` |
| POST | `/api/v1/sprints/{id}/complete` | PROJECT_LEAD | — | `SprintResponseDto` | `SPRINT_NOT_ACTIVE` |
| PATCH | `/api/v1/issues/{key}` | JWT | `{ sprintId }` | `IssueResponseDto` | `ISSUE_NOT_FOUND` |

## 5. UI / behavior

`(app)/backlog`. Collapsible sprint sections showing date range, issue count, story points,
status, and a progress bar (e.g. 8/10). Unscheduled backlog section. Actions: Create Sprint,
Start Sprint, Complete Sprint; add issue to sprint / create in backlog.

## 6. Business rules & validation

- At most **one ACTIVE sprint** per project at a time.
- Completing a sprint moves unfinished issues back to the backlog.

## 7. Acceptance criteria

- [ ] Given a planned sprint, when I start it, then it becomes ACTIVE (and no other sprint is active).
- [ ] Given an active sprint, when I complete it, then unfinished issues return to the backlog.
- [ ] Given a backlog issue, when I add it to a sprint, then it appears under that sprint.

## 8. Test cases

- **Backend:** sprint lifecycle unit (single-active rule, progress math); IT for start/complete.
- **Frontend:** Playwright — create sprint, add issues, start, complete.

## 9. Open questions

- Should completing a sprint optionally roll issues into the next planned sprint?
