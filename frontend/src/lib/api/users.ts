import { API_BASE_URL, apiFetch } from "./client";

export const ROLES = ["ADMIN", "PROJECT_LEAD", "DEVELOPER", "VIEWER"] as const;
export const STATUSES = ["ACTIVE", "INVITED", "INACTIVE"] as const;

export type Role = (typeof ROLES)[number];
export type UserStatus = (typeof STATUSES)[number];

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  status: UserStatus;
  lastActiveAt: string | null;
}

/** Mirrors the backend PageResponse<T>. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface UserFilters {
  role?: string;
  status?: string;
  projectId?: number;
  q?: string;
  page?: number;
  size?: number;
}

function toQuery(filters: UserFilters): string {
  const params = new URLSearchParams();
  if (filters.role) params.set("role", filters.role);
  if (filters.status) params.set("status", filters.status);
  if (filters.projectId != null) params.set("projectId", String(filters.projectId));
  if (filters.q) params.set("q", filters.q);
  if (filters.page != null) params.set("page", String(filters.page));
  if (filters.size != null) params.set("size", String(filters.size));
  return params.toString();
}

export function listUsers(filters: UserFilters): Promise<PageResponse<User>> {
  return apiFetch<PageResponse<User>>(`/users?${toQuery(filters)}`);
}

export interface InviteUserInput {
  name: string;
  email: string;
  role: Role;
}

export function inviteUser(input: InviteUserInput): Promise<User> {
  return apiFetch<User>("/users/invite", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function resendInvite(id: number): Promise<void> {
  return apiFetch<void>(`/users/${id}/resend-invite`, { method: "POST" });
}

/** Downloads the filtered users as CSV (not JSON, so it bypasses apiFetch). */
export async function exportUsersCsv(filters: UserFilters): Promise<Blob> {
  const response = await fetch(`${API_BASE_URL}/users/export?${toQuery(filters)}`, {
    credentials: "include",
  });
  if (!response.ok) {
    throw new Error("Export failed");
  }
  return response.blob();
}
