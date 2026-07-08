"use client";

import { useCallback, useEffect, useState } from "react";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { ApiError } from "@/lib/api/client";
import { createIssue, type IssueCard } from "@/lib/api/issues";
import { listProjects, type Project } from "@/lib/api/projects";
import {
  completeSprint,
  createSprint,
  getBacklog,
  moveIssueToSprint,
  startSprint,
  type Backlog,
  type BacklogSprint,
  type Sprint,
  type SprintStatus,
} from "@/lib/api/sprints";

const STATUS_TONE: Record<SprintStatus, "success" | "warning" | "neutral"> = {
  ACTIVE: "success",
  PLANNED: "warning",
  COMPLETED: "neutral",
};

export function BacklogView() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [projectKey, setProjectKey] = useState("");
  const [backlog, setBacklog] = useState<Backlog | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);

  useEffect(() => {
    listProjects()
      .then((list) => {
        setProjects(list);
        if (list.length > 0) setProjectKey(list[0].key);
      })
      .catch(() => setProjects([]));
  }, []);

  const load = useCallback(() => {
    if (!projectKey) return;
    setError(null);
    getBacklog(projectKey)
      .then(setBacklog)
      .catch((err: unknown) =>
        setError(err instanceof ApiError ? err.message : "Failed to load backlog"),
      );
  }, [projectKey]);

  useEffect(() => {
    load();
  }, [load]);

  const upsertSprint = (updated: Sprint) => {
    setBacklog((prev) => {
      if (!prev) return prev;
      const exists = prev.sprints.some((s) => s.sprint.id === updated.id);
      const sprints = exists
        ? prev.sprints.map((s) => (s.sprint.id === updated.id ? { ...s, sprint: updated } : s))
        : [...prev.sprints, { sprint: updated, issues: [] }];
      return { ...prev, sprints };
    });
  };

  const onStart = async (id: number) => {
    setNotice(null);
    try {
      upsertSprint(await startSprint(id));
      setNotice("Sprint started");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to start sprint");
    }
  };

  const onComplete = async (id: number) => {
    setNotice(null);
    try {
      upsertSprint(await completeSprint(id));
      setNotice("Sprint completed");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to complete sprint");
    }
  };

  const onMoveToSprint = async (issue: IssueCard, sprintId: number) => {
    setNotice(null);
    try {
      await moveIssueToSprint(issue.key, sprintId);
      setBacklog((prev) =>
        prev ? { ...prev, backlog: prev.backlog.filter((i) => i.key !== issue.key) } : prev,
      );
      setNotice(`Moved ${issue.key} to sprint`);
    } catch {
      setError("Failed to move issue");
    }
  };

  const onAddBacklogIssue = async (title: string) => {
    if (!projectKey) return;
    const issue = await createIssue({ projectKey, title, type: "TASK", priority: "MEDIUM" });
    setBacklog((prev) =>
      prev
        ? {
            ...prev,
            backlog: [
              ...prev.backlog,
              {
                id: issue.id,
                key: issue.key,
                title: issue.title,
                priority: issue.priority,
                type: issue.type,
                storyPoints: issue.storyPoints,
                assigneeId: issue.assigneeId,
                assigneeName: issue.assigneeName,
                epicName: issue.epicName,
              },
            ],
          }
        : prev,
    );
  };

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-semibold">Backlog</h1>
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
          <Button onClick={() => setCreateOpen(true)}>Create Sprint</Button>
        </div>
      </div>

      {notice && <p className="text-sm text-green-700">{notice}</p>}
      {error && (
        <p role="alert" className="text-sm text-red-600">
          {error}
        </p>
      )}

      {backlog?.sprints.map((entry) => (
        <SprintSection
          key={entry.sprint.id}
          entry={entry}
          onStart={onStart}
          onComplete={onComplete}
        />
      ))}

      <BacklogSection
        issues={backlog?.backlog ?? []}
        sprints={backlog?.sprints ?? []}
        onMoveToSprint={onMoveToSprint}
        onAddIssue={onAddBacklogIssue}
      />

      <CreateSprintModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreate={async (input) => {
          if (!projectKey) return;
          upsertSprint(await createSprint({ ...input, projectKey }));
          setCreateOpen(false);
          setNotice("Sprint created");
        }}
      />
    </section>
  );
}

function SprintSection({
  entry,
  onStart,
  onComplete,
}: {
  entry: BacklogSprint;
  onStart: (id: number) => void;
  onComplete: (id: number) => void;
}) {
  const [open, setOpen] = useState(true);
  const { sprint, issues } = entry;
  const pct = sprint.totalIssues > 0 ? (sprint.doneIssues / sprint.totalIssues) * 100 : 0;

  return (
    <div data-testid={`sprint-${sprint.id}`} className="rounded-lg border border-slate-200 bg-white">
      <div className="flex flex-wrap items-center justify-between gap-2 p-3">
        <button
          type="button"
          onClick={() => setOpen((o) => !o)}
          className="flex items-center gap-2 text-left"
        >
          <span className="text-slate-400">{open ? "▾" : "▸"}</span>
          <span className="font-semibold">{sprint.name}</span>
          <Badge tone={STATUS_TONE[sprint.status]}>{sprint.status}</Badge>
        </button>

        <div className="flex items-center gap-3 text-sm text-slate-500">
          {sprint.startDate && sprint.endDate && (
            <span>
              {sprint.startDate} → {sprint.endDate}
            </span>
          )}
          <span>{sprint.totalPoints} pts</span>
          <div className="flex items-center gap-2">
            <div className="h-2 w-24 overflow-hidden rounded-full bg-slate-200">
              <div className="h-full bg-accent" style={{ width: `${pct}%` }} />
            </div>
            <span data-testid={`progress-${sprint.id}`}>
              {sprint.doneIssues} / {sprint.totalIssues}
            </span>
          </div>
          {sprint.status === "PLANNED" && (
            <Button onClick={() => onStart(sprint.id)}>Start Sprint</Button>
          )}
          {sprint.status === "ACTIVE" && (
            <Button variant="secondary" onClick={() => onComplete(sprint.id)}>
              Complete Sprint
            </Button>
          )}
        </div>
      </div>

      {open && (
        <ul className="divide-y divide-slate-100 border-t border-slate-100">
          {issues.length === 0 ? (
            <li className="px-4 py-3 text-sm text-slate-400">No issues in this sprint</li>
          ) : (
            issues.map((issue) => <IssueRow key={issue.key} issue={issue} />)
          )}
        </ul>
      )}
    </div>
  );
}

function BacklogSection({
  issues,
  sprints,
  onMoveToSprint,
  onAddIssue,
}: {
  issues: IssueCard[];
  sprints: BacklogSprint[];
  onMoveToSprint: (issue: IssueCard, sprintId: number) => void;
  onAddIssue: (title: string) => Promise<void>;
}) {
  const [adding, setAdding] = useState(false);
  const [title, setTitle] = useState("");

  return (
    <div data-testid="backlog-section" className="rounded-lg border border-slate-200 bg-white">
      <div className="border-b border-slate-100 p-3 font-semibold">Backlog</div>
      <ul className="divide-y divide-slate-100">
        {issues.length === 0 ? (
          <li className="px-4 py-3 text-sm text-slate-400">Backlog is empty</li>
        ) : (
          issues.map((issue) => (
            <IssueRow key={issue.key} issue={issue}>
              {sprints.length > 0 && (
                <select
                  aria-label={`Move ${issue.key} to sprint`}
                  defaultValue=""
                  onChange={(e) => e.target.value && onMoveToSprint(issue, Number(e.target.value))}
                  className="rounded-md border border-slate-300 px-2 py-1 text-xs"
                >
                  <option value="">Move to sprint…</option>
                  {sprints.map((s) => (
                    <option key={s.sprint.id} value={s.sprint.id}>
                      {s.sprint.name}
                    </option>
                  ))}
                </select>
              )}
            </IssueRow>
          ))
        )}
      </ul>
      <div className="p-3">
        {adding ? (
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              if (!title.trim()) return;
              await onAddIssue(title.trim());
              setTitle("");
              setAdding(false);
            }}
          >
            <input
              autoFocus
              aria-label="New backlog issue title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-2 py-1 text-sm"
            />
          </form>
        ) : (
          <button
            type="button"
            onClick={() => setAdding(true)}
            className="text-sm text-slate-500 hover:text-slate-700"
          >
            + Add issue to backlog
          </button>
        )}
      </div>
    </div>
  );
}

function IssueRow({ issue, children }: { issue: IssueCard; children?: React.ReactNode }) {
  return (
    <li className="flex items-center justify-between px-4 py-3 text-sm">
      <div className="flex items-center gap-3">
        <Badge tone="accent">{issue.key}</Badge>
        <span>{issue.title}</span>
        {issue.storyPoints != null && <span className="text-slate-400">{issue.storyPoints} pts</span>}
      </div>
      {children}
    </li>
  );
}

function CreateSprintModal({
  open,
  onClose,
  onCreate,
}: {
  open: boolean;
  onClose: () => void;
  onCreate: (input: { name: string; goal?: string; startDate?: string; endDate?: string }) => Promise<void>;
}) {
  const [name, setName] = useState("");
  const [goal, setGoal] = useState("");
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
        name,
        goal: goal || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
      });
      setName("");
      setGoal("");
      setStartDate("");
      setEndDate("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create sprint");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="Create Sprint">
      <form onSubmit={submit} className="flex flex-col gap-3 text-sm">
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Name</span>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Goal</span>
          <input
            value={goal}
            onChange={(e) => setGoal(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2"
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
            Create sprint
          </Button>
        </div>
      </form>
    </Modal>
  );
}
