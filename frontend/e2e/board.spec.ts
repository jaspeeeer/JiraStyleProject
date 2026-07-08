import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

const BOARD = {
  projectKey: "PROJ",
  columns: [
    {
      status: "TODO",
      count: 1,
      cards: [
        {
          id: 1,
          key: "PROJ-1",
          title: "Build login",
          priority: "HIGH",
          type: "STORY",
          storyPoints: 3,
          assigneeId: null,
          assigneeName: null,
          epicName: null,
        },
      ],
    },
    { status: "IN_PROGRESS", count: 0, cards: [] },
    { status: "IN_REVIEW", count: 0, cards: [] },
    { status: "DONE", count: 0, cards: [] },
  ],
};

test.beforeEach(async ({ page }) => {
  await mockSession(page);
  await page.route("**/api/v1/projects", (route) =>
    route.fulfill({
      json: [{ id: 1, key: "PROJ", name: "Platform", description: null, issueCounter: 1 }],
    }),
  );
  await page.route("**/api/v1/projects/*/board**", (route) => route.fulfill({ json: BOARD }));
  await page.route(/\/api\/v1\/issues/, (route) => {
    const method = route.request().method();
    if (method === "POST") {
      return route.fulfill({
        status: 201,
        json: {
          id: 99,
          key: "PROJ-2",
          title: "New card",
          status: "TODO",
          priority: "MEDIUM",
          type: "STORY",
          storyPoints: null,
          assigneeId: null,
          assigneeName: null,
          epicName: null,
        },
      });
    }
    if (method === "PATCH") {
      return route.fulfill({ json: {} });
    }
    return route.continue();
  });
});

test.describe("board", () => {
  test("renders columns with counts and cards", async ({ page }) => {
    await page.goto("/board");

    await expect(page.getByRole("heading", { name: "Board" })).toBeVisible();
    await expect(page.getByTestId("column-TODO")).toContainText("To Do");
    await expect(page.getByTestId("column-IN_PROGRESS")).toContainText("In Progress");
    await expect(page.getByTestId("card-PROJ-1")).toContainText("Build login");
    await expect(page.getByTestId("count-TODO")).toHaveText("1");
  });

  test("creates an issue via the modal", async ({ page }) => {
    await page.goto("/board");
    await expect(page.getByTestId("count-TODO")).toHaveText("1");

    await page.getByRole("button", { name: "+ Create" }).click();
    await expect(page.getByRole("dialog")).toBeVisible();
    await page.getByLabel("Title").fill("New card");
    await page.getByRole("button", { name: "Create issue" }).click();

    await expect(page.getByTestId("card-PROJ-2")).toContainText("New card");
    await expect(page.getByTestId("count-TODO")).toHaveText("2");
  });

  test("drags a card to another column and counts update", async ({ page }) => {
    await page.goto("/board");
    await expect(page.getByTestId("count-TODO")).toHaveText("1");

    await page.getByTestId("card-PROJ-1").dragTo(page.getByTestId("column-IN_PROGRESS"));

    await expect(page.getByTestId("count-TODO")).toHaveText("0");
    await expect(page.getByTestId("count-IN_PROGRESS")).toHaveText("1");
    await expect(page.getByTestId("column-IN_PROGRESS")).toContainText("Build login");
  });
});
