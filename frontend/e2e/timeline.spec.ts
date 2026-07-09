import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

const TIMELINE = {
  projectKey: "PROJ",
  rangeStart: "2026-06-20",
  rangeEnd: "2026-07-20",
  sprints: [
    { id: 5, name: "Sprint 1", status: "ACTIVE", startDate: "2026-06-20", endDate: "2026-07-04" },
  ],
  items: [
    {
      key: "PROJ-1",
      title: "Build login",
      status: "IN_PROGRESS",
      type: "STORY",
      epicName: null,
      startDate: "2026-07-01",
      endDate: "2026-07-20",
    },
    {
      key: "PROJ-2",
      title: "No due date",
      status: "TODO",
      type: "TASK",
      epicName: null,
      startDate: "2026-07-02",
      endDate: "2026-07-02",
    },
  ],
};

test.beforeEach(async ({ page }) => {
  await mockSession(page);
  await page.route("**/api/v1/projects", (route) =>
    route.fulfill({
      json: [{ id: 1, key: "PROJ", name: "Platform", description: null, issueCounter: 2 }],
    }),
  );
  await page.route("**/api/v1/projects/*/timeline", (route) =>
    route.fulfill({ json: TIMELINE }),
  );
});

test.describe("timeline", () => {
  test("renders the axis, sprint band, and issue bars", async ({ page }) => {
    await page.goto("/timeline");

    await expect(page.getByRole("heading", { name: "Timeline" })).toBeVisible();
    await expect(page.getByTestId("timeline-axis")).toContainText("Jul 2026");
    await expect(page.getByTestId("timeline-sprint-5")).toContainText("Sprint 1");
    await expect(page.getByTestId("timeline-item-PROJ-1")).toContainText("Build login");
    await expect(page.getByTestId("timeline-item-PROJ-2")).toContainText("No due date");
  });

  test("shows the empty state when there is nothing to plot", async ({ page }) => {
    await page.route("**/api/v1/projects/*/timeline", (route) =>
      route.fulfill({
        json: { projectKey: "PROJ", rangeStart: null, rangeEnd: null, sprints: [], items: [] },
      }),
    );

    await page.goto("/timeline");
    await expect(page.getByText("Nothing to plot yet", { exact: false })).toBeVisible();
  });

  test("issue key links to the issue detail page", async ({ page }) => {
    await page.goto("/timeline");
    await expect(
      page.getByTestId("timeline-item-PROJ-1").getByRole("link", { name: "PROJ-1" }),
    ).toHaveAttribute("href", "/issues/PROJ-1");
  });
});
