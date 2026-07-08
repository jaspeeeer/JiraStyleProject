"use client";

import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { Avatar } from "@/components/ui/Avatar";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { ApiError } from "@/lib/api/client";
import {
  addComment,
  createSubtask,
  getIssueDetail,
  getNeighbors,
  ISSUE_STATUSES,
  PRIORITIES,
  STATUS_LABELS,
  updateIssue,
  updateSubtask,
  type IssueDetail,
  type Neighbors,
} from "@/lib/api/issues";

export function IssueDetailView({ issueKey }: { issueKey: string }) {
  const router = useRouter();
  const [detail, setDetail] = useState<IssueDetail | null>(null);
  const [neighbors, setNeighbors] = useState<Neighbors>({ prev: null, next: null });
  const [error, setError] = useState<string | null>(null);
  const [newSubtask, setNewSubtask] = useState("");
  const [newComment, setNewComment] = useState("");

  const load = useCallback(() => {
    setError(null);
    getIssueDetail(issueKey)
      .then(setDetail)
      .catch((err: unknown) =>
        setError(err instanceof ApiError ? err.message : "Failed to load issue"),
      );
    getNeighbors(issueKey)
      .then(setNeighbors)
      .catch(() => setNeighbors({ prev: null, next: null }));
  }, [issueKey]);

  useEffect(() => {
    load();
  }, [load]);

  if (error) {
    return (
      <p role="alert" className="text-sm text-red-600">
        {error}
      </p>
    );
  }
  if (!detail) {
    return <p className="text-sm text-slate-500">Loading…</p>;
  }

  const { issue, subtasks, comments, activity, subtasksDone, subtasksTotal } = detail;
  const pct = subtasksTotal > 0 ? (subtasksDone / subtasksTotal) * 100 : 0;

  const onField = async (patch: Parameters<typeof updateIssue>[1]) => {
    await updateIssue(issueKey, patch);
    load();
  };

  const onToggleSubtask = async (id: number, done: boolean) => {
    await updateSubtask(id, { done });
    load();
  };

  const onAddSubtask = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!newSubtask.trim()) return;
    await createSubtask(issueKey, newSubtask.trim());
    setNewSubtask("");
    load();
  };

  const onAddComment = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!newComment.trim()) return;
    await addComment(issueKey, newComment.trim());
    setNewComment("");
    load();
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Badge tone="accent">{issue.key}</Badge>
          <span className="text-sm text-slate-500">{issue.type}</span>
        </div>
        <div className="flex gap-2">
          <Button
            variant="secondary"
            disabled={!neighbors.prev}
            onClick={() => neighbors.prev && router.push(`/issues/${neighbors.prev}`)}
          >
            ← Prev
          </Button>
          <Button
            variant="secondary"
            disabled={!neighbors.next}
            onClick={() => neighbors.next && router.push(`/issues/${neighbors.next}`)}
          >
            Next →
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_280px]">
        {/* Main column */}
        <div className="flex flex-col gap-6">
          <h1 className="text-2xl font-semibold">{issue.title}</h1>

          <section>
            <h2 className="mb-1 text-sm font-semibold text-slate-500">Description</h2>
            <p className="whitespace-pre-wrap text-sm text-slate-700">
              {issue.description ?? "No description"}
            </p>
          </section>

          {/* Subtasks */}
          <section className="flex flex-col gap-2">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold text-slate-500">Subtasks</h2>
              <span data-testid="subtask-progress" className="text-xs text-slate-500">
                {subtasksDone} / {subtasksTotal}
              </span>
            </div>
            <div className="h-2 w-full overflow-hidden rounded-full bg-slate-200">
              <div className="h-full bg-accent" style={{ width: `${pct}%` }} />
            </div>
            <ul className="flex flex-col gap-1">
              {subtasks.map((s) => (
                <li key={s.id} data-testid={`subtask-${s.id}`} className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    checked={s.done}
                    aria-label={`Toggle ${s.title}`}
                    onChange={(e) => onToggleSubtask(s.id, e.target.checked)}
                  />
                  <span className={s.done ? "text-slate-400 line-through" : ""}>{s.title}</span>
                  {s.assigneeName && <Avatar name={s.assigneeName} />}
                </li>
              ))}
            </ul>
            <form onSubmit={onAddSubtask}>
              <input
                aria-label="New subtask"
                placeholder="Add a subtask…"
                value={newSubtask}
                onChange={(e) => setNewSubtask(e.target.value)}
                className="w-full rounded-md border border-slate-300 px-2 py-1 text-sm"
              />
            </form>
          </section>

          {/* Comments */}
          <section className="flex flex-col gap-2">
            <h2 className="text-sm font-semibold text-slate-500">Comments</h2>
            <ul className="flex flex-col gap-2">
              {comments.map((c) => (
                <li key={c.id} className="rounded-md border border-slate-200 p-2 text-sm">
                  <span className="font-medium">{c.authorName ?? "Unknown"}</span>
                  <p className="text-slate-700">{c.body}</p>
                </li>
              ))}
            </ul>
            <form onSubmit={onAddComment} className="flex flex-col gap-2">
              <textarea
                aria-label="New comment"
                placeholder="Add a comment…"
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                className="w-full rounded-md border border-slate-300 px-2 py-1 text-sm"
              />
              <div>
                <Button type="submit">Comment</Button>
              </div>
            </form>
          </section>

          {/* Activity */}
          <section className="flex flex-col gap-2">
            <h2 className="text-sm font-semibold text-slate-500">Activity</h2>
            <ul className="flex flex-col gap-1 text-xs text-slate-500">
              {activity.map((a) => (
                <li key={a.id}>
                  <span className="font-medium text-slate-600">{a.actorName ?? "System"}</span>{" "}
                  {a.action}
                  {a.field ? ` ${a.field}: ${a.oldValue ?? "—"} → ${a.newValue ?? "—"}` : ""}
                </li>
              ))}
            </ul>
          </section>
        </div>

        {/* Metadata panel */}
        <aside className="flex flex-col gap-4 rounded-lg border border-slate-200 bg-white p-4 text-sm">
          <Meta label="Status">
            <select
              aria-label="Status"
              value={issue.status}
              onChange={(e) => onField({ status: e.target.value as never })}
              className="rounded-md border border-slate-300 px-2 py-1"
            >
              {ISSUE_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {STATUS_LABELS[s]}
                </option>
              ))}
            </select>
          </Meta>
          <Meta label="Priority">
            <select
              aria-label="Priority"
              value={issue.priority}
              onChange={(e) => onField({ priority: e.target.value as never })}
              className="rounded-md border border-slate-300 px-2 py-1"
            >
              {PRIORITIES.map((p) => (
                <option key={p} value={p}>
                  {p}
                </option>
              ))}
            </select>
          </Meta>
          <Meta label="Assignee">{issue.assigneeName ?? "Unassigned"}</Meta>
          <Meta label="Reporter">{issue.reporterName ?? "—"}</Meta>
          <Meta label="Story Points">{issue.storyPoints ?? "—"}</Meta>
          <Meta label="Sprint">{issue.sprintName ?? "—"}</Meta>
          <Meta label="Epic">{issue.epicName ?? "—"}</Meta>
          <Meta label="Due Date">{issue.dueDate ?? "—"}</Meta>
          <Meta label="Created">{new Date(issue.createdAt).toLocaleDateString()}</Meta>
        </aside>
      </div>
    </div>
  );
}

function Meta({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs uppercase tracking-wide text-slate-400">{label}</span>
      <span className="text-slate-700">{children}</span>
    </div>
  );
}
