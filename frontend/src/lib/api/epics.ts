import { apiFetch } from "./client";

export interface Epic {
  id: number;
  key: string;
  name: string;
  color: string | null;
  description: string | null;
  startDate: string | null;
  endDate: string | null;
}

export function listEpics(projectKey: string): Promise<Epic[]> {
  return apiFetch<Epic[]>(`/projects/${projectKey}/epics`);
}

export interface CreateEpicInput {
  projectKey: string;
  name: string;
  color?: string;
  description?: string;
  startDate?: string;
  endDate?: string;
}

export function createEpic(input: CreateEpicInput): Promise<Epic> {
  return apiFetch<Epic>("/epics", {
    method: "POST",
    body: JSON.stringify(input),
  });
}
