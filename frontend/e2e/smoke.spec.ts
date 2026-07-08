import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

test.beforeEach(async ({ page }) => {
  await mockSession(page);
});

test.describe("app shell smoke", () => {
  test("root redirects to the board and shows the sidebar", async ({ page }) => {
    await page.goto("/");
    await expect(page).toHaveURL(/\/board$/);
    await expect(page.getByRole("heading", { name: "Board" })).toBeVisible();
    await expect(page.getByRole("link", { name: "Backlog" })).toBeVisible();
  });

  test("sidebar navigates between sections", async ({ page }) => {
    await page.goto("/board");
    await page.getByRole("link", { name: "Backlog" }).click();
    await expect(page).toHaveURL(/\/backlog$/);
    await expect(page.getByRole("heading", { name: "Backlog" })).toBeVisible();
  });

  test("login route renders without the app shell", async ({ page }) => {
    await page.goto("/login");
    await expect(page.getByRole("heading", { name: "InTrack" })).toBeVisible();
    // Sidebar nav should NOT be present on the auth route.
    await expect(page.getByRole("link", { name: "Board" })).toHaveCount(0);
  });
});
