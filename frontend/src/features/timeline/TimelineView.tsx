"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { Badge } from "@/components/ui/Badge";
import { useProjects } from "@/features/projects/useProjects";
import { ApiError } from "@/lib/api/client";
import { getTimeline, type Timeline } from "@/lib/api/insights";
import type { IssueStatus } from "@/lib/api/issues";
import { monthTicks, spanPercent } from "@/lib/utils/timescale";

/** Bar color per issue status (Tailwind tokens, mirroring the badge tones). */
const STATUS_BAR: Record<IssueStatus, string> = {
  TODO: "bg-slate-300",
  IN_PROGRESS: "bg-accent",
  IN_REVIEW: "bg-amber-400",
  DONE: "bg-green-500",
};

export function TimelineView() {
  const { projects, projectKey, setProjectKey } = useProjects();
  const [timeline, setTimeline] = useState<Timeline | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    if (!projectKey) return;
    setError(null);
    getTimeline(projectKey)
      .then(setTimeline)
      .catch((err: unknown) =>
        setError(err instanceof ApiError ? err.message : "Failed to load timeline"),
      );
  }, [projectKey]);

  useEffect(() => {
    load();
  }, [load]);

  const rangeStart = timeline?.rangeStart ?? null;
  const rangeEnd = timeline?.rangeEnd ?? null;

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-semibold">Timeline</h1>
        <select
          aria-label="Project"
          value={projectKey}
          onChange={(e) => setProjectKey(e.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
        >
          {projects.map((p) => (
            <option key={p.id} value={p.key}>
              {p.name}
            </option>
          ))}
        </select>
      </div>

      {error && (
        <p role="alert" className="text-sm text-red-600">
          {error}
        </p>
      )}

      {timeline && !rangeStart && (
        <div className="rounded-lg border border-dashed border-slate-300 bg-white p-10 text-center text-sm text-slate-400">
          Nothing to plot yet — create issues or sprints with dates.
        </div>
      )}

      {timeline && rangeStart && rangeEnd && (
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          {/* Month axis */}
          <div className="mb-2 grid grid-cols-[240px_1fr] gap-3">
            <div />
            <div data-testid="timeline-axis" className="relative h-5 border-b border-slate-200">
              {monthTicks(rangeStart, rangeEnd).map((tick) => (
                <span
                  key={tick.label + tick.left}
                  className="absolute top-0 text-xs text-slate-400"
                  style={{ left: `${tick.left}%` }}
                >
                  {tick.label}
                </span>
              ))}
            </div>
          </div>

          {/* Sprint bands */}
          {timeline.sprints.map((sprint) => {
            const span = spanPercent(sprint.startDate, sprint.endDate, rangeStart, rangeEnd);
            return (
              <div
                key={sprint.id}
                data-testid={`timeline-sprint-${sprint.id}`}
                className="mb-1 grid grid-cols-[240px_1fr] items-center gap-3"
              >
                <div className="flex items-center gap-2 text-sm">
                  <span className="truncate text-slate-500">{sprint.name}</span>
                  <Badge tone={sprint.status === "ACTIVE" ? "success" : "neutral"}>
                    {sprint.status}
                  </Badge>
                </div>
                <div className="relative h-5 rounded bg-slate-50">
                  <div
                    title={`${sprint.startDate} → ${sprint.endDate}`}
                    className="absolute inset-y-0.5 rounded border border-slate-300 bg-slate-200"
                    style={{ left: `${span.left}%`, width: `${span.width}%` }}
                  />
                </div>
              </div>
            );
          })}

          {/* Issue bars */}
          <div className="mt-2 flex flex-col gap-1 border-t border-slate-100 pt-2">
            {timeline.items.map((item) => {
              const span = spanPercent(item.startDate, item.endDate, rangeStart, rangeEnd);
              return (
                <div
                  key={item.key}
                  data-testid={`timeline-item-${item.key}`}
                  className="grid grid-cols-[240px_1fr] items-center gap-3"
                >
                  <div className="flex items-center gap-2 text-sm">
                    <Link href={`/issues/${item.key}`} className="hover:underline">
                      <Badge tone="accent">{item.key}</Badge>
                    </Link>
                    <span className="truncate">{item.title}</span>
                  </div>
                  <div className="relative h-6 rounded bg-slate-50">
                    <div
                      title={`${item.startDate} → ${item.endDate}`}
                      className={`absolute inset-y-1.5 rounded-full ${STATUS_BAR[item.status]}`}
                      style={{ left: `${span.left}%`, width: `${span.width}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </section>
  );
}
