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
