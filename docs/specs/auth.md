# Auth — Login / Register

- **Status:** Shipped (Phase 2)
- **Owner:** TBD
- **Related specs:** [[users]]
- **Last updated:** 2026-07-08

> Implemented in Phase 2: JWT over httpOnly cookies, register/login/refresh/logout/me,
> forgot/reset password (single-use tokens, logged email), SSO stub. Refresh is stateless
> (no server-side rotation store yet).

## 1. Purpose & scope

Email + password authentication with JWT sessions for an internal-use tool. Sign In and Create
Account tabs, forgot-password, and a **stubbed** "Continue with SSO" secondary path.
**Out of scope:** real IdP/SSO integration; public self-service signup beyond internal invite.

## 2. User stories

- As an internal user, I want to sign in with email + password so that I can access InTrack.
- As a new invitee, I want to create an account so that I can be onboarded.
- As a user, I want to reset a forgotten password so that I can regain access.

## 3. Data model

Touches `User` (email, passwordHash, status). Refresh tokens / password-reset tokens modeled in
Phase 2. Enums: `UserStatus (ACTIVE, INVITED, INACTIVE)`. See [[users]].

## 4. API contract

| Method | Path | Auth | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | none | `RegisterRequestDto` | `TokenResponseDto` | `EMAIL_TAKEN` |
| POST | `/api/v1/auth/login` | none | `LoginRequestDto` | `TokenResponseDto` | `BAD_CREDENTIALS` |
| POST | `/api/v1/auth/refresh` | cookie | — | `TokenResponseDto` | `INVALID_TOKEN` |
| POST | `/api/v1/auth/logout` | cookie | — | 204 | — |
| POST | `/api/v1/auth/forgot-password` | none | `ForgotPasswordRequestDto` | 204 | — |
| POST | `/api/v1/auth/reset-password` | none | `ResetPasswordRequestDto` | 204 | `INVALID_TOKEN` |
| GET | `/api/v1/auth/sso` | none | — | redirect (stub) | — |
| GET | `/api/v1/auth/me` | JWT | — | `UserResponseDto` | `UNAUTHORIZED` |

JWT is delivered as an httpOnly secure cookie + refresh-token rotation.

## 5. UI / behavior

`(auth)/login` route (no app shell). Tabs: **Sign In** / **Create Account**. Primary gold
"Sign In" button; secondary "Continue with SSO"; "Forgot password" and "Request access" links.
States: loading, invalid-credentials error, success → redirect to `/board`.

## 6. Business rules & validation

- Email format validated; password min length enforced.
- Only `ACTIVE`/`INVITED` users may complete login/registration.
- SSO button hits the stub endpoint (no real IdP yet).

## 7. Acceptance criteria

- [ ] Given valid credentials, when I sign in, then I receive a session and land on the board.
- [ ] Given wrong credentials, when I sign in, then I see an error and no session is set.
- [ ] Given a valid invite, when I create an account, then my status becomes `ACTIVE`.

## 8. Test cases

- **Backend:** `JwtTokenProvider` unit; `AuthServiceImpl` unit (bad credentials, email taken);
  IT for login/register/refresh/me happy + error paths.
- **Frontend:** Playwright — sign in success, sign in failure, register, forgot-password flow.

## 9. Open questions

- Password policy specifics (length/complexity)?
- Refresh-token lifetime and rotation storage?
