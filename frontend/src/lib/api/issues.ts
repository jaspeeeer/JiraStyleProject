import { apiFetch } from "./client";

export const ISSUE_STATUSES = ["TODO", "IN_PROGRESS", "IN_REVIEW", "DONE"] as const;
export const PRIORITIES = ["HIGH", "MEDIUM", "LOW"] as const;
export const ISSUE_TYPES = ["STORY", "BUG", "TASK", "EPIC"] as const;

export type IssueStatus = (typeof ISSUE_STATUSES)[number];
export type Priority = (typeof PRIORITIES)[number];
export type IssueType = (typeof ISSUE_TYPES)[number];

export const STATUS_LABELS: Record<IssueStatus, string> = {
  TODO: "To Do",
  IN_PROGRESS: "In Progress",
  IN_REVIEW: "In Review",
  DONE: "Done",
};

export interface IssueCard {
  id: number;
  key: string;
  title: string;
  priority: Priority;
  type: IssueType;
  storyPoints: number | null;
  assigneeId: number | null;
  assigneeName: string | null;
  epicName: string | null;
}

export interface BoardColumn {
  status: IssueStatus;
  count: number;
  cards: IssueCard[];
}

export interface Board {
  projectKey: string;
  columns: BoardColumn[];
}

/** Full issue (subset of the backend IssueResponseDto used on the board). */
export interface Issue {
  id: number;
  key: string;
  title: string;
  status: IssueStatus;
  priority: Priority;
  type: IssueType;
  storyPoints: number | null;
  assigneeId: number | null;
  assigneeName: string | null;
  epicName: string | null;
}

export interface BoardFilters {
  priority?: string;
  type?: string;
  assigneeId?: number;
  epicId?: number;
}

export function getBoard(projectKey: string, filters: BoardFilters = {}): Promise<Board> {
  const params = new URLSearchParams();
  if (filters.priority) params.set("priority", filters.priority);
  if (filters.type) params.set("type", filters.type);
  if (filters.assigneeId != null) params.set("assigneeId", String(filters.assigneeId));
  if (filters.epicId != null) params.set("epicId", String(filters.epicId));
  return apiFetch<Board>(`/projects/${projectKey}/board?${params.toString()}`);
}

export interface CreateIssueInput {
  projectKey: string;
  title: string;
  type: IssueType;
  priority: Priority;
  status?: IssueStatus;
  storyPoints?: number | null;
  description?: string;
}

export function createIssue(input: CreateIssueInput): Promise<Issue> {
  return apiFetch<Issue>("/issues", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateIssueStatus(key: string, status: IssueStatus): Promise<Issue> {
  return apiFetch<Issue>(`/issues/${key}`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
  });
}
