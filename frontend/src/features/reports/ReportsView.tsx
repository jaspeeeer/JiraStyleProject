"use client";

import { useCallback, useEffect, useState } from "react";
import { Badge } from "@/components/ui/Badge";
import { useProjects } from "@/features/projects/useProjects";
import { ApiError } from "@/lib/api/client";
import { getReports, type CountItem, type Reports } from "@/lib/api/insights";

export function ReportsView() {
  const { projects, projectKey, setProjectKey } = useProjects();
  const [reports, setReports] = useState<Reports | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    if (!projectKey) return;
    setError(null);
    getReports(projectKey)
      .then(setReports)
      .catch((err: unknown) =>
        setError(err instanceof ApiError ? err.message : "Failed to load reports"),
      );
  }, [projectKey]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-semibold">Reports</h1>
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

      {reports && (
        <>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
            <StatTile label="Total issues" value={reports.totalIssues} testId="stat-total-issues" />
            <StatTile label="Done issues" value={reports.doneIssues} testId="stat-done-issues" />
            <StatTile label="Total points" value={reports.totalPoints} testId="stat-total-points" />
            <StatTile label="Done points" value={reports.donePoints} testId="stat-done-points" />
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
            <DistributionChart title="By status" items={reports.statusCounts} />
            <DistributionChart title="By priority" items={reports.priorityCounts} />
            <DistributionChart title="By type" items={reports.typeCounts} />
          </div>

          <div className="rounded-lg border border-slate-200 bg-white p-4">
            <h2 className="mb-3 text-sm font-semibold text-slate-600">
              Velocity (story points per sprint)
            </h2>
            {reports.velocity.length === 0 ? (
              <p className="text-sm text-slate-400">No sprints yet.</p>
            ) : (
              <div className="flex flex-col gap-2">
                {reports.velocity.map((point) => {
                  const maxTotal = Math.max(...reports.velocity.map((v) => v.totalPoints), 1);
                  return (
                    <div
                      key={point.sprintId}
                      data-testid={`velocity-${point.sprintId}`}
                      className="grid grid-cols-[160px_1fr_110px] items-center gap-3 text-sm"
                    >
                      <div className="flex items-center gap-2 truncate">
                        <span className="truncate">{point.name}</span>
                        <Badge tone={point.status === "COMPLETED" ? "success" : "warning"}>
                          {point.status}
                        </Badge>
                      </div>
                      <div className="relative h-4 rounded-full bg-slate-100">
                        <div
                          className="absolute inset-y-0 rounded-full bg-slate-300"
                          style={{ width: `${(point.totalPoints / maxTotal) * 100}%` }}
                        />
                        <div
                          className="absolute inset-y-0 rounded-full bg-accent"
                          style={{ width: `${(point.completedPoints / maxTotal) * 100}%` }}
                        />
                      </div>
                      <span className="text-right text-xs text-slate-500">
                        {point.completedPoints} / {point.totalPoints} pts
                      </span>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </>
      )}
    </section>
  );
}

function StatTile({
  label,
  value,
  testId,
}: {
  label: string;
  value: number;
  testId: string;
}) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <div className="text-xs uppercase tracking-wide text-slate-400">{label}</div>
      <div data-testid={testId} className="mt-1 text-2xl font-semibold text-slate-900">
        {value}
      </div>
    </div>
  );
}

function DistributionChart({ title, items }: { title: string; items: CountItem[] }) {
  const max = Math.max(...items.map((item) => item.count), 1);
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <h2 className="mb-3 text-sm font-semibold text-slate-600">{title}</h2>
      <div className="flex flex-col gap-2">
        {items.map((item) => (
          <div key={item.label} className="flex items-center gap-2 text-sm">
            <span className="w-28 truncate text-xs text-slate-500">{item.label}</span>
            <div className="h-3 flex-1 rounded-full bg-slate-100">
              <div
                className="h-full rounded-full bg-accent"
                style={{ width: `${(item.count / max) * 100}%` }}
              />
            </div>
            <span className="w-8 text-right text-xs text-slate-500">{item.count}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
