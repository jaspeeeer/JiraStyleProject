import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

test.beforeEach(async ({ page }) => {
  await mockSession(page);
});

/**
 * Exercises the projects vertical slice end-to-end on the client: the page calls the typed API
 * module, which hits the backend. The backend response is mocked here so the test runs without a
 * live server (and exercises the Table + Badge primitives).
 */
test.describe("projects page", () => {
  test("renders projects returned by the API in a table", async ({ page }) => {
    await page.route("**/api/v1/projects", async (route) => {
      await route.fulfill({
        json: [
          { id: 1, key: "PROJ", name: "Platform", description: "core", issueCounter: 3 },
          { id: 2, key: "MOB", name: "Mobile App", description: null, issueCounter: 0 },
        ],
      });
    });

    await page.goto("/projects");

    await expect(page.getByRole("heading", { name: "Projects" })).toBeVisible();
    await expect(page.getByText("Platform")).toBeVisible();
    await expect(page.getByText("Mobile App")).toBeVisible();
    await expect(page.getByText("PROJ", { exact: true })).toBeVisible();
    await expect(page.getByText("MOB", { exact: true })).toBeVisible();
  });

  test("shows an error state when the API fails", async ({ page }) => {
    await page.route("**/api/v1/projects", async (route) => {
      await route.fulfill({
        status: 500,
        json: {
          timestamp: "2026-07-08T00:00:00Z",
          status: 500,
          code: "INTERNAL",
          message: "boom",
          details: [],
          path: "/api/v1/projects",
        },
      });
    });

    await page.goto("/projects");
    await expect(page.getByRole("alert")).toBeVisible();
  });
});
