import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

test.beforeEach(async ({ page }) => {
  await mockSession(page);
});

test.describe("UI primitives", () => {
  test("buttons and badges render", async ({ page }) => {
    await page.goto("/dev/ui");
    await expect(page.getByRole("button", { name: "Primary" })).toBeVisible();
    await expect(page.getByText("Accent")).toBeVisible();
    await expect(page.getByText("AA")).toBeVisible(); // Avatar initials for "Ada Admin"
  });

  test("tabs switch the active tab", async ({ page }) => {
    await page.goto("/dev/ui");
    await expect(page.getByTestId("active-tab")).toHaveText("Active tab: one");
    await page.getByRole("tab", { name: "Create Account" }).click();
    await expect(page.getByTestId("active-tab")).toHaveText("Active tab: two");
  });

  test("modal opens and closes", async ({ page }) => {
    await page.goto("/dev/ui");
    await expect(page.getByRole("dialog")).toHaveCount(0);
    await page.getByRole("button", { name: "Open modal" }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByRole("button", { name: "Close" }).click();
    await expect(page.getByRole("dialog")).toHaveCount(0);
  });
});
