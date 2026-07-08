import { expect, test } from "@playwright/test";
import { mockSession } from "./support/session";

const PAGES = [
  { path: "/roadmap", heading: "Roadmap", endpoint: "roadmap", message: "Roadmap is not built yet — planned for a later phase." },
  { path: "/reports", heading: "Reports", endpoint: "reports", message: "Reports are not built yet — planned for a later phase." },
  { path: "/timeline", heading: "Timeline", endpoint: "timeline", message: "Timeline is not built yet — planned for a later phase." },
];

test.beforeEach(async ({ page }) => {
  await mockSession(page);
  await page.route("**/api/v1/projects", (route) =>
    route.fulfill({ json: [{ id: 1, key: "PROJ", name: "Platform", description: null, issueCounter: 0 }] }),
  );
  for (const p of PAGES) {
    await page.route(`**/api/v1/projects/*/${p.endpoint}`, (route) =>
      route.fulfill({ json: { projectKey: "PROJ", status: "SCAFFOLD", message: p.message } }),
    );
  }
});

test.describe("scaffold pages (roadmap / reports / timeline)", () => {
  for (const p of PAGES) {
    test(`${p.heading} loads and shows the stub message`, async ({ page }) => {
      await page.goto(p.path);
      await expect(page.getByRole("heading", { name: p.heading })).toBeVisible();
      await expect(page.getByTestId("scaffold-message")).toHaveText(p.message);
    });
  }

  test("sidebar navigates between the scaffold pages (no dead links)", async ({ page }) => {
    await page.goto("/roadmap");
    await page.getByRole("link", { name: "Reports" }).click();
    await expect(page).toHaveURL(/\/reports$/);
    await page.getByRole("link", { name: "Timeline" }).click();
    await expect(page).toHaveURL(/\/timeline$/);
  });
});
