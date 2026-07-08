import type { Page } from "@playwright/test";

export const SESSION_USER = {
  id: 1,
  name: "Ada Admin",
  email: "admin@intrack.local",
  role: "ADMIN",
  status: "ACTIVE",
  lastActiveAt: null,
};

/** Mocks an authenticated session so pages behind the AuthGuard render. */
export async function mockSession(page: Page): Promise<void> {
  await page.route("**/api/v1/auth/me", (route) => route.fulfill({ json: SESSION_USER }));
}
