import { apiFetch } from "./client";
import type { IssueStatus, IssueType } from "./issues";
import type { SprintStatus } from "./sprints";

// --- Roadmap ---

export interface RoadmapEpic {
  id: number;
  key: string;
  name: string;
  color: string | null;
  startDate: string | null;
  endDate: string | null;
  totalIssues: number;
  doneIssues: number;
}

export interface Roadmap {
  projectKey: string;
  epics: RoadmapEpic[];
}

export function getRoadmap(projectKey: string): Promise<Roadmap> {
  return apiFetch<Roadmap>(`/projects/${projectKey}/roadmap`);
}

// --- Reports ---

export interface CountItem {
  label: string;
  count: number;
}

export interface VelocityPoint {
  sprintId: number;
  name: string;
  status: SprintStatus;
  completedPoints: number;
  totalPoints: number;
}

export interface Reports {
  projectKey: string;
  totalIssues: number;
  doneIssues: number;
  totalPoints: number;
  donePoints: number;
  statusCounts: CountItem[];
  priorityCounts: CountItem[];
  typeCounts: CountItem[];
  velocity: VelocityPoint[];
}

export function getReports(projectKey: string): Promise<Reports> {
  return apiFetch<Reports>(`/projects/${projectKey}/reports`);
}

// --- Timeline ---

export interface TimelineSprint {
  id: number;
  name: string;
  status: SprintStatus;
  startDate: string;
  endDate: string;
}

export interface TimelineItem {
  key: string;
  title: string;
  status: IssueStatus;
  type: IssueType;
  epicName: string | null;
  startDate: string;
  endDate: string;
}

export interface Timeline {
  projectKey: string;
  rangeStart: string | null;
  rangeEnd: string | null;
  sprints: TimelineSprint[];
  items: TimelineItem[];
}

export function getTimeline(projectKey: string): Promise<Timeline> {
  return apiFetch<Timeline>(`/projects/${projectKey}/timeline`);
}
