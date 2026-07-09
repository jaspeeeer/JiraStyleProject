"use client";

import { useCallback, useEffect, useState } from "react";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { useProjects } from "@/features/projects/useProjects";
import { ApiError } from "@/lib/api/client";
import { createEpic } from "@/lib/api/epics";
import { getRoadmap, type Roadmap, type RoadmapEpic } from "@/lib/api/insights";
import { monthTicks, spanPercent } from "@/lib/utils/timescale";

export function RoadmapView() {
  const { projects, projectKey, setProjectKey } = useProjects();
  const [roadmap, setRoadmap] = useState<Roadmap | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);

  const load = useCallback(() => {
    if (!projectKey) return;
    setError(null);
    getRoadmap(projectKey)
      .then(setRoadmap)
      .catch((err: unknown) =>
        setError(err instanceof ApiError ? err.message : "Failed to load roadmap"),
      );
  }, [projectKey]);

  useEffect(() => {
    load();
  }, [load]);

  const scheduled = (roadmap?.epics ?? []).filter((e) => e.startDate && e.endDate);
  const unscheduled = (roadmap?.epics ?? []).filter((e) => !e.startDate || !e.endDate);

  // ISO dates compare correctly as strings.
  const rangeStart = scheduled.map((e) => e.startDate!).sort()[0];
  const rangeEnd = scheduled.map((e) => e.endDate!).sort().at(-1);

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-semibold">Roadmap</h1>
        <div className="flex gap-2">
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
          <Button onClick={() => setCreateOpen(true)}>New Epic</Button>
        </div>
      </div>

      {error && (
        <p role="alert" className="text-sm text-red-600">
          {error}
        </p>
      )}

      {roadmap && roadmap.epics.length === 0 && (
        <div className="rounded-lg border border-dashed border-slate-300 bg-white p-10 text-center text-sm text-slate-400">
          No epics yet — create one to start planning the roadmap.
        </div>
      )}

      {scheduled.length > 0 && rangeStart && rangeEnd && (
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          {/* Month axis */}
          <div className="mb-2 grid grid-cols-[240px_1fr] gap-3">
            <div />
            <div data-testid="roadmap-axis" className="relative h-5 border-b border-slate-200">
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

          <div className="flex flex-col gap-2">
            {scheduled.map((epic) => (
              <EpicRow key={epic.id} epic={epic} rangeStart={rangeStart} rangeEnd={rangeEnd} />
            ))}
          </div>
        </div>
      )}

      {unscheduled.length > 0 && (
        <div className="rounded-lg border border-slate-200 bg-white">
          <div className="border-b border-slate-100 p-3 text-sm font-semibold text-slate-600">
            Unscheduled
          </div>
          <ul className="divide-y divide-slate-100">
            {unscheduled.map((epic) => (
              <li
                key={epic.id}
                data-testid={`epic-row-${epic.key}`}
                className="flex items-center gap-3 px-4 py-3 text-sm"
              >
                <Badge tone="accent">{epic.key}</Badge>
                <span className="font-medium">{epic.name}</span>
                <span className="text-xs text-slate-400">
                  {epic.doneIssues} / {epic.totalIssues} done
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}

      <CreateEpicModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreate={async (input) => {
          if (!projectKey) return;
          await createEpic({ ...input, projectKey });
          setCreateOpen(false);
          load();
        }}
      />
    </section>
  );
}

function EpicRow({
  epic,
  rangeStart,
  rangeEnd,
}: {
  epic: RoadmapEpic;
  rangeStart: string;
  rangeEnd: string;
}) {
  const span = spanPercent(epic.startDate!, epic.endDate!, rangeStart, rangeEnd);
  const donePct = epic.totalIssues > 0 ? (epic.doneIssues / epic.totalIssues) * 100 : 0;

  return (
    <div
      data-testid={`epic-row-${epic.key}`}
      className="grid grid-cols-[240px_1fr] items-center gap-3"
    >
      <div className="flex items-center gap-2 text-sm">
        <Badge tone="accent">{epic.key}</Badge>
        <span className="truncate font-medium">{epic.name}</span>
        <span data-testid={`epic-progress-${epic.key}`} className="text-xs text-slate-400">
          {epic.doneIssues} / {epic.totalIssues}
        </span>
      </div>
      <div className="relative h-6 rounded bg-slate-50">
        <div
          title={`${epic.startDate} → ${epic.endDate}`}
          className="absolute inset-y-1 rounded-full bg-accent"
          style={{
            left: `${span.left}%`,
            width: `${span.width}%`,
            ...(epic.color ? { backgroundColor: epic.color } : {}),
          }}
        >
          {/* Done fraction overlay */}
          {epic.totalIssues > 0 && (
            <div
              className="h-full rounded-full bg-white/40"
              style={{ width: `${donePct}%` }}
            />
          )}
        </div>
      </div>
    </div>
  );
}

function CreateEpicModal({
  open,
  onClose,
  onCreate,
}: {
  open: boolean;
  onClose: () => void;
  onCreate: (input: {
    name: string;
    color?: string;
    startDate?: string;
    endDate?: string;
  }) => Promise<void>;
}) {
  const [name, setName] = useState("");
  const [color, setColor] = useState("#d4a72c"); // default epic color: the accent gold
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await onCreate({
        name: name.trim(),
        color,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
      });
      setName("");
      setStartDate("");
      setEndDate("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create epic");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="New Epic">
      <form onSubmit={submit} className="flex flex-col gap-3 text-sm">
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Name</span>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2"
          />
        </label>
        <label className="flex items-center gap-2">
          <span className="text-slate-600">Color</span>
          <input
            type="color"
            aria-label="Color"
            value={color}
            onChange={(e) => setColor(e.target.value)}
            className="h-8 w-12 cursor-pointer rounded border border-slate-300"
          />
        </label>
        <div className="flex gap-2">
          <label className="flex flex-1 flex-col gap-1">
            <span className="text-slate-600">Start</span>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="rounded-md border border-slate-300 px-3 py-2"
            />
          </label>
          <label className="flex flex-1 flex-col gap-1">
            <span className="text-slate-600">End</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="rounded-md border border-slate-300 px-3 py-2"
            />
          </label>
        </div>

        {error && (
          <p role="alert" className="text-red-600">
            {error}
          </p>
        )}

        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" disabled={submitting}>
            Create epic
          </Button>
        </div>
      </form>
    </Modal>
  );
}
