import { apiFetch } from "./client";

/** Placeholder payload from a scaffold endpoint (roadmap / reports / timeline). */
export interface ScaffoldInfo {
  projectKey: string;
  status: string;
  message: string;
}

export function getRoadmap(projectKey: string): Promise<ScaffoldInfo> {
  return apiFetch<ScaffoldInfo>(`/projects/${projectKey}/roadmap`);
}

export function getReports(projectKey: string): Promise<ScaffoldInfo> {
  return apiFetch<ScaffoldInfo>(`/projects/${projectKey}/reports`);
}

export function getTimeline(projectKey: string): Promise<ScaffoldInfo> {
  return apiFetch<ScaffoldInfo>(`/projects/${projectKey}/timeline`);
}
