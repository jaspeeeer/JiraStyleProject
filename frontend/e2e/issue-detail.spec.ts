import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

function baseIssue(key: string, title: string, status: string) {
  return {
    id: key === "PROJ-1" ? 1 : 2,
    key,
    projectKey: "PROJ",
    title,
    description: "Some description",
    status,
    priority: "MEDIUM",
    type: "STORY",
    storyPoints: 3,
    dueDate: null,
    assigneeId: null,
    assigneeName: null,
    reporterId: 1,
    reporterName: "Ada",
    epicId: null,
    epicName: null,
    sprintId: null,
    sprintName: null,
    createdAt: "2026-07-08T00:00:00Z",
    updatedAt: "2026-07-08T00:00:00Z",
  };
}

test.beforeEach(async ({ page }) => {
  await mockSession(page);

  // Small in-memory model so edits reflect on reload.
  const proj1 = {
    status: "TODO",
    subtasks: [
      { id: 10, title: "Wire form", done: false, assigneeId: null, assigneeName: null, orderIndex: 0 },
    ],
    comments: [
      { id: 20, body: "Looks good", authorId: 1, authorName: "Ada", createdAt: "2026-07-08T00:00:00Z" },
    ],
  };
  let nextSubtaskId = 100;
  let nextCommentId = 200;

  const proj1Detail = () => ({
    issue: baseIssue("PROJ-1", "Build login", proj1.status),
    subtasksDone: proj1.subtasks.filter((s) => s.done).length,
    subtasksTotal: proj1.subtasks.length,
    subtasks: proj1.subtasks,
    comments: proj1.comments,
    activity: [
      { id: 30, action: "CREATED", field: null, oldValue: null, newValue: null, actorName: "Ada", createdAt: "2026-07-08T00:00:00Z" },
    ],
  });
  const proj2Detail = {
    issue: baseIssue("PROJ-2", "Ship logout", "TODO"),
    subtasksDone: 0,
    subtasksTotal: 0,
    subtasks: [],
    comments: [],
    activity: [],
  };

  await page.route(/\/api\/v1\/(issues|subtasks)/, (route) => {
    const req = route.request();
    const url = req.url();
    const method = req.method();

    if (url.includes("/subtasks/")) {
      // toggle subtask done
      const body = JSON.parse(req.postData() || "{}");
      if (typeof body.done === "boolean") proj1.subtasks[0].done = body.done;
      return route.fulfill({ json: proj1.subtasks[0] });
    }
    if (url.endsWith("/subtasks")) {
      const body = JSON.parse(req.postData() || "{}");
      const sub = { id: nextSubtaskId++, title: body.title, done: false, assigneeId: null, assigneeName: null, orderIndex: proj1.subtasks.length };
      proj1.subtasks.push(sub);
      return route.fulfill({ status: 201, json: sub });
    }
    if (url.endsWith("/comments")) {
      const body = JSON.parse(req.postData() || "{}");
      const comment = { id: nextCommentId++, body: body.body, authorId: 1, authorName: "Ada", createdAt: "2026-07-08T00:00:00Z" };
      proj1.comments.push(comment);
      return route.fulfill({ status: 201, json: comment });
    }
    if (url.includes("/neighbors")) {
      const isP1 = url.includes("PROJ-1");
      return route.fulfill({ json: isP1 ? { prev: null, next: "PROJ-2" } : { prev: "PROJ-1", next: null } });
    }
    // detail GET or issue PATCH
    const isP1 = url.includes("PROJ-1");
    if (method === "PATCH") {
      const body = JSON.parse(req.postData() || "{}");
      if (body.status) proj1.status = body.status;
      return route.fulfill({ json: baseIssue("PROJ-1", "Build login", proj1.status) });
    }
    return route.fulfill({ json: isP1 ? proj1Detail() : proj2Detail });
  });
});

test.describe("issue detail", () => {
  test("renders the aggregate", async ({ page }) => {
    await page.goto("/issues/PROJ-1");

    await expect(page.getByRole("heading", { name: "Build login" })).toBeVisible();
    await expect(page.getByTestId("subtask-progress")).toHaveText("0 / 1");
    await expect(page.getByText("Wire form")).toBeVisible();
    await expect(page.getByText("Looks good")).toBeVisible();
    await expect(page.getByLabel("Status")).toHaveValue("TODO");
  });

  test("edits the status field", async ({ page }) => {
    await page.goto("/issues/PROJ-1");
    await page.getByLabel("Status").selectOption("IN_PROGRESS");
    await expect(page.getByLabel("Status")).toHaveValue("IN_PROGRESS");
  });

  test("toggles a subtask and progress updates", async ({ page }) => {
    await page.goto("/issues/PROJ-1");
    await expect(page.getByTestId("subtask-progress")).toHaveText("0 / 1");
    await page.getByLabel("Toggle Wire form").click();
    await expect(page.getByTestId("subtask-progress")).toHaveText("1 / 1");
  });

  test("adds a comment", async ({ page }) => {
    await page.goto("/issues/PROJ-1");
    await page.getByLabel("New comment").fill("Nice work");
    await page.getByRole("button", { name: "Comment" }).click();
    await expect(page.getByText("Nice work")).toBeVisible();
  });

  test("navigates to the next issue", async ({ page }) => {
    await page.goto("/issues/PROJ-1");
    await page.getByRole("button", { name: "Next →" }).click();
    await expect(page).toHaveURL(/\/issues\/PROJ-2$/);
    await expect(page.getByRole("heading", { name: "Ship logout" })).toBeVisible();
  });
});
