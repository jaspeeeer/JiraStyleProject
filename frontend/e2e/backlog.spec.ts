import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

const SPRINT1 = {
  id: 1,
  name: "Sprint 1",
  goal: "Ship auth",
  status: "PLANNED",
  startDate: "2026-07-01",
  endDate: "2026-07-14",
  totalIssues: 2,
  doneIssues: 1,
  totalPoints: 5,
};

const BACKLOG = {
  projectKey: "PROJ",
  sprints: [
    {
      sprint: SPRINT1,
      issues: [
        { id: 1, key: "PROJ-1", title: "Build login", priority: "HIGH", type: "STORY", storyPoints: 3, assigneeId: null, assigneeName: null, epicName: null },
        { id: 2, key: "PROJ-2", title: "Logout", priority: "LOW", type: "TASK", storyPoints: 2, assigneeId: null, assigneeName: null, epicName: null },
      ],
    },
  ],
  backlog: [
    { id: 3, key: "PROJ-3", title: "Backlog item", priority: "MEDIUM", type: "TASK", storyPoints: null, assigneeId: null, assigneeName: null, epicName: null },
  ],
};

test.beforeEach(async ({ page }) => {
  await mockSession(page);
  await page.route("**/api/v1/projects", (route) =>
    route.fulfill({ json: [{ id: 1, key: "PROJ", name: "Platform", description: null, issueCounter: 3 }] }),
  );
  await page.route("**/api/v1/projects/*/backlog", (route) => route.fulfill({ json: BACKLOG }));
  await page.route(/\/api\/v1\/sprints/, (route) => {
    const url = route.request().url();
    if (url.endsWith("/start")) return route.fulfill({ json: { ...SPRINT1, status: "ACTIVE" } });
    if (url.endsWith("/complete")) return route.fulfill({ json: { ...SPRINT1, status: "COMPLETED" } });
    return route.fulfill({
      status: 201,
      json: { id: 2, name: "Sprint 2", goal: null, status: "PLANNED", startDate: null, endDate: null, totalIssues: 0, doneIssues: 0, totalPoints: 0 },
    });
  });
});

test.describe("backlog & sprints", () => {
  test("renders sprints with progress and the backlog", async ({ page }) => {
    await page.goto("/backlog");

    await expect(page.getByRole("heading", { name: "Backlog" })).toBeVisible();
    await expect(page.getByTestId("sprint-1")).toContainText("Sprint 1");
    await expect(page.getByTestId("progress-1")).toHaveText("1 / 2");
    await expect(page.getByTestId("sprint-1")).toContainText("Build login");
    await expect(page.getByTestId("backlog-section")).toContainText("Backlog item");
  });

  test("creates a sprint", async ({ page }) => {
    await page.goto("/backlog");
    await page.getByRole("button", { name: "Create Sprint" }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByLabel("Name").fill("Sprint 2");
    await page.getByRole("button", { name: "Create sprint", exact: true }).click();

    await expect(page.getByTestId("sprint-2")).toContainText("Sprint 2");
  });

  test("starts then completes a sprint", async ({ page }) => {
    await page.goto("/backlog");

    await page.getByRole("button", { name: "Start Sprint" }).click();
    await expect(page.getByTestId("sprint-1")).toContainText("ACTIVE");

    await page.getByRole("button", { name: "Complete Sprint" }).click();
    await expect(page.getByTestId("sprint-1")).toContainText("COMPLETED");
  });
});
