# Projects

- **Status:** Shipped (list + create)
- **Owner:** TBD
- **Related specs:** [[board]], [[backlog-sprints]], [[issue-detail]]
- **Last updated:** 2026-07-08

## 1. Purpose & scope

Projects are the top-level container for issues, sprints, and epics. Each has a short **key**
(e.g. `PROJ`) used as the prefix for issue keys (`PROJ-1`). This spec covers listing projects and
creating a new one. **Out of scope (for now):** edit/archive/delete, per-project settings,
membership management UI.

## 2. User stories

- As a user, I want to see all projects so that I can pick one to work in.
- As an admin / project lead, I want to create a project so that a team can start tracking work.

## 3. Data model

`Project` (id, `projectKey` unique, name, description, `issueCounter`), audited timestamps.
Schema from Flyway `V2__init_core.sql` (table `projects`); entity `ProjectEntity`.

## 4. API contract

| Method | Path | Auth | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/projects` | JWT | — | `ProjectResponseDto[]` | — |
| POST | `/api/v1/projects` | ADMIN or PROJECT_LEAD | `CreateProjectRequestDto` | `ProjectResponseDto` (201) | `VALIDATION`, `PROJECT_KEY_TAKEN` |

`CreateProjectRequestDto`: `key` (2–10 uppercase letters/digits, starts with a letter, unique),
`name` (required), `description` (optional).

## 5. UI / behavior

`(app)/projects`: a table of Key / Name / Description / Issues with a gold **New Project** button.
The button opens a modal (Key, Name, Description). On submit the new project is created and
appended to the table; a duplicate key or invalid input shows an inline error in the modal.

## 6. Business rules & validation

- `key` must match `^[A-Z][A-Z0-9]{1,9}$` and be unique (else `PROJECT_KEY_TAKEN` → 409).
- Only ADMIN or PROJECT_LEAD may create projects (403 otherwise).
- New projects start with `issueCounter = 0`.

## 7. Acceptance criteria

- [x] Given the projects page, the table lists all projects.
- [x] Given ADMIN/PROJECT_LEAD, when I submit a valid new project, it is created and appears in the table.
- [x] Given a duplicate key, when I submit, I see a 409 error and no project is created.
- [x] Given a non-privileged role, the create endpoint returns 403.

## 8. Test cases

- **Backend:** `ProjectControllerIT` — create (201), duplicate key (409), non-privileged role (403), list.
- **Frontend:** Playwright — list renders; create via modal appends the row; API error state.

## 9. Open questions

- Should creating a project auto-add the creator as a member / set a default lead?
- Edit / archive project — future.
