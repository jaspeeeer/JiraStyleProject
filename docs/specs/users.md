# User Management — Users & Permissions

- **Status:** Shipped (Phase 2)
- **Owner:** TBD
- **Related specs:** [[auth]]
- **Last updated:** 2026-07-08

> Implemented in Phase 2: ADMIN-only paginated/filterable users list (role, status, project,
> search), invite + resend, CSV export, role/status update, roles list. Authz enforced via
> method security with an IT 403 matrix.

## 1. Purpose & scope

Admin management of users: a searchable, filterable, paginated table; invite flow; CSV export;
resend-invite. **Out of scope:** granular per-resource permissions beyond the four roles.

## 2. User stories

- As an admin, I want to search/filter users so that I can find accounts quickly.
- As an admin, I want to invite a user so that they can join.
- As an admin, I want to export users to CSV so that I can report on access.

## 3. Data model

`User` (name, email, role, status, lastActiveAt), M:N `Project` membership. Enums:
`Role (ADMIN, PROJECT_LEAD, DEVELOPER, VIEWER)`, `UserStatus (ACTIVE, INVITED, INACTIVE)`.
Schema created in Flyway `V2__init_core.sql` (tables `users`, `project_members`); entity `UserEntity`.

## 4. API contract

| Method | Path | Auth | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/v1/users` | ADMIN | filters `role,status,project,q,page,size,sort` | `PageResponse<UserResponseDto>` | — |
| POST | `/api/v1/users/invite` | ADMIN | `InviteUserRequestDto` | `UserResponseDto` | `EMAIL_TAKEN` |
| POST | `/api/v1/users/{id}/resend-invite` | ADMIN | — | 204 | `USER_NOT_FOUND` |
| GET | `/api/v1/users/export` | ADMIN | filters | `text/csv` | — |
| PATCH | `/api/v1/users/{id}` | ADMIN | `UpdateUserRequestDto` (role/status) | `UserResponseDto` | `USER_NOT_FOUND` |
| GET | `/api/v1/roles` | ADMIN | — | `RoleResponseDto[]` | — |

## 5. UI / behavior

`(app)/admin/users`. Columns: name, email, role, status, last-active. Filters: Role, Status,
Project + free-text search. Pagination. Actions: **Invite User** (modal), **Export CSV**,
inline **resend-invite** on `INVITED` rows.

## 6. Business rules & validation

- Only `ADMIN` may access any endpoint here (403 otherwise).
- Resend-invite only valid for `INVITED` users.

## 7. Acceptance criteria

- [ ] Given filters, when I apply them, then the table shows only matching users, paginated.
- [ ] Given a new email, when I invite, then a user appears with status `INVITED`.
- [ ] Given a non-admin, when they call any endpoint, then they get 403.

## 8. Test cases

- **Backend:** service unit (filter/spec building, invite dedupe); IT incl. **403 authz matrix**
  per role; CSV export IT.
- **Frontend:** Playwright — filter, invite, resend, export triggers download.

## 9. Open questions

- CSV column set and formatting?
