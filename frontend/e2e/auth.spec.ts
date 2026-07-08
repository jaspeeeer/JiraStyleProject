import { expect, test } from "@playwright/test";
import { SESSION_USER } from "./support/session";

test.describe("auth", () => {
  test("sign in succeeds and lands on the board", async ({ page }) => {
    await page.route("**/api/v1/auth/login", (route) =>
      route.fulfill({ json: SESSION_USER }),
    );
    await page.route("**/api/v1/auth/me", (route) => route.fulfill({ json: SESSION_USER }));

    await page.goto("/login");
    await page.getByLabel("Email").fill("admin@intrack.local");
    await page.getByLabel("Password").fill("password");
    await page.getByRole("button", { name: "Sign In" }).click();

    await expect(page).toHaveURL(/\/board$/);
    await expect(page.getByRole("heading", { name: "Board" })).toBeVisible();
  });

  test("shows an error on bad credentials", async ({ page }) => {
    await page.route("**/api/v1/auth/login", (route) =>
      route.fulfill({
        status: 401,
        json: {
          timestamp: "2026-07-08T00:00:00Z",
          status: 401,
          code: "BAD_CREDENTIALS",
          message: "Invalid email or password",
          details: [],
          path: "/api/v1/auth/login",
        },
      }),
    );

    await page.goto("/login");
    await page.getByLabel("Email").fill("admin@intrack.local");
    await page.getByLabel("Password").fill("wrong");
    await page.getByRole("button", { name: "Sign In" }).click();

    await expect(page.getByText("Invalid email or password")).toBeVisible();
  });

  test("create account tab reveals the name field", async ({ page }) => {
    await page.goto("/login");
    await expect(page.getByLabel("Name")).toHaveCount(0);
    await page.getByRole("tab", { name: "Create Account" }).click();
    await expect(page.getByLabel("Name")).toBeVisible();
  });
});
