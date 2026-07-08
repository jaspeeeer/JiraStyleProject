import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

const USER_PAGE = {
  content: [
    { id: 1, name: "Ada Admin", email: "admin@intrack.local", role: "ADMIN", status: "ACTIVE", lastActiveAt: null },
    { id: 2, name: "Ivy Invited", email: "ivy@intrack.local", role: "VIEWER", status: "INVITED", lastActiveAt: null },
  ],
  page: 0,
  size: 10,
  totalElements: 2,
  totalPages: 1,
};

test.beforeEach(async ({ page }) => {
  await mockSession(page);
  await page.route("**/api/v1/projects", (route) => route.fulfill({ json: [] }));
  await page.route(/\/api\/v1\/users/, (route) => {
    const request = route.request();
    if (request.url().includes("/users/invite")) {
      return route.fulfill({
        status: 201,
        json: { id: 3, name: "New Bie", email: "newbie@intrack.local", role: "DEVELOPER", status: "INVITED", lastActiveAt: null },
      });
    }
    return route.fulfill({ json: USER_PAGE });
  });
});

test.describe("users admin", () => {
  test("lists users with a resend action for invited users", async ({ page }) => {
    await page.goto("/admin/users");

    await expect(page.getByRole("heading", { name: "Users & Permissions" })).toBeVisible();
    await expect(page.getByText("Ada Admin")).toBeVisible();
    await expect(page.getByText("Ivy Invited")).toBeVisible();
    await expect(page.getByRole("button", { name: "Resend" })).toBeVisible();
    await expect(page.getByText("2 users")).toBeVisible();
  });

  test("invites a new user", async ({ page }) => {
    await page.goto("/admin/users");

    await page.getByRole("button", { name: "Invite User" }).click();
    await expect(page.getByRole("dialog")).toBeVisible();

    await page.getByLabel("Name").fill("New Bie");
    await page.getByLabel("Email").fill("newbie@intrack.local");
    await page.getByRole("button", { name: "Send invite" }).click();

    await expect(page.getByText("Invitation sent to newbie@intrack.local")).toBeVisible();
  });
});
