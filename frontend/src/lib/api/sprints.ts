import { apiFetch } from "./client";
import type { IssueCard } from "./issues";

export const SPRINT_STATUSES = ["PLANNED", "ACTIVE", "COMPLETED"] as const;
export type SprintStatus = (typeof SPRINT_STATUSES)[number];

export interface Sprint {
  id: number;
  name: string;
  goal: string | null;
  status: SprintStatus;
  startDate: string | null;
  endDate: string | null;
  totalIssues: number;
  doneIssues: number;
  totalPoints: number;
}

export interface BacklogSprint {
  sprint: Sprint;
  issues: IssueCard[];
}

export interface Backlog {
  projectKey: string;
  sprints: BacklogSprint[];
  backlog: IssueCard[];
}

export function getBacklog(projectKey: string): Promise<Backlog> {
  return apiFetch<Backlog>(`/projects/${projectKey}/backlog`);
}

export interface CreateSprintInput {
  projectKey: string;
  name: string;
  goal?: string;
  startDate?: string;
  endDate?: string;
}

export function createSprint(input: CreateSprintInput): Promise<Sprint> {
  return apiFetch<Sprint>("/sprints", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function startSprint(id: number): Promise<Sprint> {
  return apiFetch<Sprint>(`/sprints/${id}/start`, { method: "POST" });
}

export function completeSprint(id: number): Promise<Sprint> {
  return apiFetch<Sprint>(`/sprints/${id}/complete`, { method: "POST" });
}

/** Moves an issue into a sprint (backlog → sprint) via the issue PATCH endpoint. */
export function moveIssueToSprint(issueKey: string, sprintId: number): Promise<unknown> {
  return apiFetch(`/issues/${issueKey}`, {
    method: "PATCH",
    body: JSON.stringify({ sprintId }),
  });
}
