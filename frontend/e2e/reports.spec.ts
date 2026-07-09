import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

const REPORTS = {
  projectKey: "PROJ",
  totalIssues: 6,
  doneIssues: 2,
  totalPoints: 21,
  donePoints: 8,
  statusCounts: [
    { label: "TODO", count: 2 },
    { label: "IN_PROGRESS", count: 1 },
    { label: "IN_REVIEW", count: 1 },
    { label: "DONE", count: 2 },
  ],
  priorityCounts: [
    { label: "HIGH", count: 3 },
    { label: "MEDIUM", count: 2 },
    { label: "LOW", count: 1 },
  ],
  typeCounts: [
    { label: "STORY", count: 4 },
    { label: "BUG", count: 1 },
    { label: "TASK", count: 1 },
    { label: "EPIC", count: 0 },
  ],
  velocity: [
    { sprintId: 1, name: "Sprint 1", status: "COMPLETED", completedPoints: 8, totalPoints: 10 },
    { sprintId: 2, name: "Sprint 2", status: "ACTIVE", completedPoints: 0, totalPoints: 11 },
  ],
};

test.beforeEach(async ({ page }) => {
  await mockSession(page);
  await page.route("**/api/v1/projects", (route) =>
    route.fulfill({
      json: [{ id: 1, key: "PROJ", name: "Platform", description: null, issueCounter: 6 }],
    }),
  );
  await page.route("**/api/v1/projects/*/reports", (route) =>
    route.fulfill({ json: REPORTS }),
  );
});

test.describe("reports", () => {
  test("shows headline stat tiles", async ({ page }) => {
    await page.goto("/reports");

    await expect(page.getByRole("heading", { name: "Reports" })).toBeVisible();
    await expect(page.getByTestId("stat-total-issues")).toHaveText("6");
    await expect(page.getByTestId("stat-done-issues")).toHaveText("2");
    await expect(page.getByTestId("stat-total-points")).toHaveText("21");
    await expect(page.getByTestId("stat-done-points")).toHaveText("8");
  });

  test("shows distributions and per-sprint velocity", async ({ page }) => {
    await page.goto("/reports");

    await expect(page.getByText("By status")).toBeVisible();
    await expect(page.getByText("By priority")).toBeVisible();
    await expect(page.getByTestId("velocity-1")).toContainText("Sprint 1");
    await expect(page.getByTestId("velocity-1")).toContainText("8 / 10 pts");
    await expect(page.getByTestId("velocity-2")).toContainText("0 / 11 pts");
  });
});
