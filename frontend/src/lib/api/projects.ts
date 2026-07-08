import { apiFetch } from "./client";

/** Mirrors the backend ProjectResponseDto. */
export interface Project {
  id: number;
  key: string;
  name: string;
  description: string | null;
  issueCounter: number;
}

export function listProjects(): Promise<Project[]> {
  return apiFetch<Project[]>("/projects");
}

export interface CreateProjectInput {
  key: string;
  name: string;
  description?: string;
}

export function createProject(input: CreateProjectInput): Promise<Project> {
  return apiFetch<Project>("/projects", {
    method: "POST",
    body: JSON.stringify(input),
  });
}
