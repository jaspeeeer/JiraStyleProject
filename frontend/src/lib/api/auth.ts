import { apiFetch } from "./client";

/** Mirrors the backend UserResponseDto (the authenticated principal). */
export interface SessionUser {
  id: number;
  name: string;
  email: string;
  role: string;
  status: string;
  lastActiveAt: string | null;
}

export function login(email: string, password: string): Promise<SessionUser> {
  return apiFetch<SessionUser>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function register(
  name: string,
  email: string,
  password: string,
): Promise<SessionUser> {
  return apiFetch<SessionUser>("/auth/register", {
    method: "POST",
    body: JSON.stringify({ name, email, password }),
  });
}

export function me(): Promise<SessionUser> {
  return apiFetch<SessionUser>("/auth/me");
}

export function logout(): Promise<void> {
  return apiFetch<void>("/auth/logout", { method: "POST" });
}

export function forgotPassword(email: string): Promise<void> {
  return apiFetch<void>("/auth/forgot-password", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}

export function resetPassword(token: string, newPassword: string): Promise<void> {
  return apiFetch<void>("/auth/reset-password", {
    method: "POST",
    body: JSON.stringify({ token, newPassword }),
  });
}
