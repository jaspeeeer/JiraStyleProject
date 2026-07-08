# Issue Detail

- **Status:** Shipped (Phase 5)
- **Owner:** TBD
- **Related specs:** [[board]], [[backlog-sprints]]
- **Last updated:** 2026-07-08

> Implemented in Phase 5: detail aggregate (`GET /issues/{key}` → issue + subtasks + comments +
> activity + progress), subtask CRUD, comments (author = current user), activity feed, prev/next
> neighbors. UI: editable status/priority, subtasks with progress + toggle, comments, activity,
> metadata panel, prev/next nav; board card key links here. Verified live end-to-end (add subtask,
> toggle → 1/1, add comment) against the running backend + Postgres. Description editing and
> per-subtask assignee selection in the UI are deferred (fields display; backend supports both).

## 1. Purpose & scope

Full issue view: editable fields, rich description, subtasks with progress and per-subtask
assignees, comments + activity feed, a metadata panel, and prev/next navigation.
**Out of scope:** issue linking / dependencies (later).

## 2. User stories

- As a user, I want to edit an issue's fields so that details stay accurate.
- As a user, I want subtasks with a progress bar so that I can track breakdown.
- As a user, I want to comment so that I can collaborate, with an activity trail.

## 3. Data model

`Issue` + 1:N `Subtask` (title, done, assignee, orderIndex), 1:N `Comment` (body, author),
1:N `ActivityLog` (actor, action, field, oldValue, newValue). M:N `Label`; N:1 `Epic`, `Sprint`.
Schema created in Flyway `V2__init_core.sql` (tables `subtasks`, `comments`, `activity_logs`,
`labels`, `epics`); entities `SubtaskEntity`, `CommentEntity`, `ActivityLogEntity`.

## 4. API contract

| Method | Path | Auth | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/issues/{key}` | JWT | — | `IssueDetailResponseDto` | `ISSUE_NOT_FOUND` |
| PATCH | `/api/v1/issues/{key}` | JWT | `UpdateIssueRequestDto` | `IssueResponseDto` | `ISSUE_NOT_FOUND` |
| GET | `/api/v1/issues/{key}/neighbors` | JWT | — | `{ prev, next }` | — |
| POST | `/api/v1/issues/{key}/subtasks` | JWT | `CreateSubtaskRequestDto` | `SubtaskResponseDto` | — |
| PATCH | `/api/v1/subtasks/{id}` | JWT | `UpdateSubtaskRequestDto` | `SubtaskResponseDto` | `SUBTASK_NOT_FOUND` |
| DELETE | `/api/v1/subtasks/{id}` | JWT | — | 204 | `SUBTASK_NOT_FOUND` |
| GET/POST | `/api/v1/issues/{key}/comments` | JWT | `CreateCommentRequestDto` | `CommentResponseDto` | — |
| GET | `/api/v1/issues/{key}/activity` | JWT | — | `ActivityResponseDto[]` | — |

## 5. UI / behavior

`(app)/issues/[key]`. Title, status dropdown, priority, epic, labels, rich description.
Subtasks list with completion progress bar + per-subtask assignee. Comments/activity feed.
Right metadata panel: Status, Assignee, Reporter, Priority, Story Points, Sprint, Epic, Labels,
Due Date, Created. Prev/Next navigation.

## 6. Business rules & validation

- Field changes append an `ActivityLog` entry.
- Subtask progress = done/total.

## 7. Acceptance criteria

- [ ] Given an issue, when I change a field, then it persists and an activity entry appears.
- [ ] Given a subtask, when I toggle it done, then the progress bar updates.
- [ ] Given a comment, when I post it, then it appears in the feed.
- [ ] Given prev/next, when I navigate, then the adjacent issue loads.

## 8. Test cases

- **Backend:** unit (progress calc, activity logging); IT for detail/subtask/comment/neighbors.
- **Frontend:** Playwright — edit field, add + toggle subtask, comment, prev/next nav.

## 9. Open questions

- Rich-text format for description (Markdown vs limited HTML)?
