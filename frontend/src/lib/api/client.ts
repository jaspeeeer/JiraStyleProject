/**
 * Typed fetch client for the InTrack backend. All feature API modules build on this — components
 * never call `fetch` with raw URLs directly (CLAUDE.md §11).
 */

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

/** Mirrors the backend error envelope (CLAUDE.md §9). */
export interface ApiErrorBody {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  details: string[];
  path: string;
}

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly details: string[];

  constructor(body: ApiErrorBody) {
    super(body.message);
    this.status = body.status;
    this.code = body.code;
    this.details = body.details;
  }
}

export async function apiFetch<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
    credentials: "include", // send the session cookie (Phase 2)
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiErrorBody | null;
    throw new ApiError(
      body ?? {
        timestamp: new Date().toISOString(),
        status: response.status,
        code: "UNKNOWN",
        message: response.statusText,
        details: [],
        path,
      },
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
