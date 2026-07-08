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

// --- Issue detail (Phase 5) ---

/** Full issue view (mirrors backend IssueResponseDto). */
export interface IssueFull {
  id: number;
  key: string;
  projectKey: string;
  title: string;
  description: string | null;
  status: IssueStatus;
  priority: Priority;
  type: IssueType;
  storyPoints: number | null;
  dueDate: string | null;
  assigneeId: number | null;
  assigneeName: string | null;
  reporterId: number | null;
  reporterName: string | null;
  epicId: number | null;
  epicName: string | null;
  sprintId: number | null;
  sprintName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Subtask {
  id: number;
  title: string;
  done: boolean;
  assigneeId: number | null;
  assigneeName: string | null;
  orderIndex: number;
}

export interface Comment {
  id: number;
  body: string;
  authorId: number | null;
  authorName: string | null;
  createdAt: string;
}

export interface Activity {
  id: number;
  action: string;
  field: string | null;
  oldValue: string | null;
  newValue: string | null;
  actorName: string | null;
  createdAt: string;
}

export interface IssueDetail {
  issue: IssueFull;
  subtasksDone: number;
  subtasksTotal: number;
  subtasks: Subtask[];
  comments: Comment[];
  activity: Activity[];
}

export interface Neighbors {
  prev: string | null;
  next: string | null;
}

export type IssuePatch = Partial<{
  title: string;
  description: string;
  status: IssueStatus;
  priority: Priority;
  type: IssueType;
  storyPoints: number;
}>;

export function getIssueDetail(key: string): Promise<IssueDetail> {
  return apiFetch<IssueDetail>(`/issues/${key}`);
}

export function getNeighbors(key: string): Promise<Neighbors> {
  return apiFetch<Neighbors>(`/issues/${key}/neighbors`);
}

export function updateIssue(key: string, patch: IssuePatch): Promise<IssueFull> {
  return apiFetch<IssueFull>(`/issues/${key}`, {
    method: "PATCH",
    body: JSON.stringify(patch),
  });
}

export function createSubtask(key: string, title: string): Promise<Subtask> {
  return apiFetch<Subtask>(`/issues/${key}/subtasks`, {
    method: "POST",
    body: JSON.stringify({ title }),
  });
}

export function updateSubtask(id: number, patch: { done?: boolean; title?: string }): Promise<Subtask> {
  return apiFetch<Subtask>(`/subtasks/${id}`, {
    method: "PATCH",
    body: JSON.stringify(patch),
  });
}

export function addComment(key: string, body: string): Promise<Comment> {
  return apiFetch<Comment>(`/issues/${key}/comments`, {
    method: "POST",
    body: JSON.stringify({ body }),
  });
}
