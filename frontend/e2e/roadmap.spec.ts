import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

const EPIC_SCHEDULED = {
  id: 1,
  key: "E1",
  name: "Auth overhaul",
  color: "#d4a72c",
  startDate: "2026-07-01",
  endDate: "2026-08-15",
  totalIssues: 4,
  doneIssues: 1,
};

const EPIC_UNSCHEDULED = {
  id: 2,
  key: "E2",
  name: "Someday ideas",
  color: null,
  startDate: null,
  endDate: null,
  totalIssues: 0,
  doneIssues: 0,
};

test.beforeEach(async ({ page }) => {
  await mockSession(page);
  await page.route("**/api/v1/projects", (route) =>
    route.fulfill({
      json: [{ id: 1, key: "PROJ", name: "Platform", description: null, issueCounter: 4 }],
    }),
  );

  // Mutable epic list so a created epic appears on the roadmap reload.
  const epics = [EPIC_SCHEDULED, EPIC_UNSCHEDULED];
  await page.route("**/api/v1/projects/*/roadmap", (route) =>
    route.fulfill({ json: { projectKey: "PROJ", epics } }),
  );
  await page.route("**/api/v1/epics", (route) => {
    const body = JSON.parse(route.request().postData() || "{}");
    const created = {
      id: 3,
      key: "E3",
      name: body.name,
      color: body.color ?? null,
      startDate: body.startDate ?? null,
      endDate: body.endDate ?? null,
      totalIssues: 0,
      doneIssues: 0,
    };
    epics.push(created);
    return route.fulfill({ status: 201, json: created });
  });
});

test.describe("roadmap", () => {
  test("renders scheduled epics with progress and a month axis", async ({ page }) => {
    await page.goto("/roadmap");

    await expect(page.getByRole("heading", { name: "Roadmap" })).toBeVisible();
    await expect(page.getByTestId("roadmap-axis")).toContainText("Jul 2026");
    await expect(page.getByTestId("epic-row-E1")).toContainText("Auth overhaul");
    await expect(page.getByTestId("epic-progress-E1")).toHaveText("1 / 4");
  });

  test("groups undated epics under Unscheduled", async ({ page }) => {
    await page.goto("/roadmap");

    await expect(page.getByText("Unscheduled", { exact: true })).toBeVisible();
    await expect(page.getByTestId("epic-row-E2")).toContainText("Someday ideas");
  });

  test("creates an epic via the modal", async ({ page }) => {
    await page.goto("/roadmap");

    await page.getByRole("button", { name: "New Epic" }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByLabel("Name").fill("Payments");
    await page.getByRole("button", { name: "Create epic", exact: true }).click();

    await expect(page.getByTestId("epic-row-E3")).toContainText("Payments");
  });
});
