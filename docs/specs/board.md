# Board — Kanban

- **Status:** Shipped (Phase 3)
- **Owner:** TBD
- **Related specs:** [[issue-detail]], [[backlog-sprints]]
- **Last updated:** 2026-07-08

> Implemented in Phase 3: issue create/get/update, per-project key generation, board endpoint
> grouped by status with priority/type/assignee/epic filters, status-change activity logging,
> and a Kanban UI with drag-and-drop (optimistic + PATCH). **Frontend filters currently expose
> Priority and Type + a Group-by control; Assignee/Epic filters are deferred** until the
> user/epic option-list endpoints exist (epics land in Phase 4).

## 1. Purpose & scope

Kanban board with columns **To Do / In Progress / In Review / Done**, per-column counts,
filters, group-by, issue creation, and drag-and-drop between columns.
**Out of scope:** swimlane customization, WIP limits (later).

## 2. User stories

- As a developer, I want to drag a card between columns so that its status updates.
- As a user, I want to filter the board so that I see only relevant issues.
- As a user, I want to create an issue from the board so that I can capture work quickly.

## 3. Data model

`Issue` (key, title, status, priority, type, storyPoints, assignee, epic). Enums:
`IssueStatus (TODO, IN_PROGRESS, IN_REVIEW, DONE)`, `Priority (HIGH, MEDIUM, LOW)`,
`IssueType (STORY, BUG, TASK, EPIC)`. Schema created in Flyway `V2__init_core.sql`
(tables `issues`, `issue_labels`); entity `IssueEntity`.

## 4. API contract

| Method | Path | Auth | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/projects/{key}/board` | JWT | filters `assignee,priority,epic,type,groupBy` | `BoardResponseDto` (columns+cards+counts) | `PROJECT_NOT_FOUND` |
| POST | `/api/v1/issues` | JWT | `CreateIssueRequestDto` | `IssueResponseDto` | `VALIDATION` |
| PATCH | `/api/v1/issues/{key}` | JWT | `UpdateIssueRequestDto` (incl. `status`) | `IssueResponseDto` | `ISSUE_NOT_FOUND` |

Drag-and-drop persists via `PATCH /issues/{key}` with the new `status`.

## 5. UI / behavior

`(app)/board`. Columns with counts; cards show key, title, priority, assignee avatar, story
points. Filters: Assignee, Priority, Epic, Type. "Group by" control. "+ Create" and inline
"+ Add issue" per column. DnD moves cards; on drop the card optimistically moves then persists.

## 6. Business rules & validation

- Status transitions: any column → any column allowed (no workflow gating in v1).
- Issue key is generated per project (`<PROJECTKEY>-<n>`).

## 7. Acceptance criteria

- [ ] Given a board, when I drag a card to another column, then its status persists and counts update.
- [ ] Given filters, when applied, then only matching cards show.
- [ ] Given "+ Create", when I submit, then a new card appears in the target column.

## 8. Test cases

- **Backend:** service unit (key generation, status change, activity log); board endpoint IT (filters).
- **Frontend:** Playwright — create issue, filter, drag card across columns + count update.

## 9. Open questions

- Group-by dimensions to support in v1 (assignee, epic, priority)?
