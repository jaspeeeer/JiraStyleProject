"use client";

import { useCallback, useEffect, useState } from "react";
import { Avatar } from "@/components/ui/Avatar";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { ApiError } from "@/lib/api/client";
import {
  createIssue,
  getBoard,
  ISSUE_TYPES,
  PRIORITIES,
  STATUS_LABELS,
  updateIssueStatus,
  type Board,
  type IssueCard,
  type IssueStatus,
  type IssueType,
  type Priority,
} from "@/lib/api/issues";
import { listProjects, type Project } from "@/lib/api/projects";

const PRIORITY_TONE: Record<Priority, "danger" | "warning" | "neutral"> = {
  HIGH: "danger",
  MEDIUM: "warning",
  LOW: "neutral",
};

type GroupBy = "none" | "priority" | "type" | "assignee";

export function BoardView() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [projectKey, setProjectKey] = useState<string>("");
  const [board, setBoard] = useState<Board | null>(null);
  const [priority, setPriority] = useState("");
  const [type, setType] = useState("");
  const [groupBy, setGroupBy] = useState<GroupBy>("none");
  const [dragging, setDragging] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listProjects()
      .then((list) => {
        setProjects(list);
        if (list.length > 0) setProjectKey(list[0].key);
      })
      .catch(() => setProjects([]));
  }, []);

  const loadBoard = useCallback(() => {
    if (!projectKey) return;
    setError(null);
    getBoard(projectKey, { priority: priority || undefined, type: type || undefined })
      .then(setBoard)
      .catch((err: unknown) =>
        setError(err instanceof ApiError ? err.message : "Failed to load board"),
      );
  }, [projectKey, priority, type]);

  useEffect(() => {
    loadBoard();
  }, [loadBoard]);

  const mutateColumns = (fn: (columns: Board["columns"]) => Board["columns"]) => {
    setBoard((prev) => (prev ? { ...prev, columns: fn(prev.columns) } : prev));
  };

  const addCard = (status: IssueStatus, card: IssueCard) => {
    mutateColumns((columns) =>
      columns.map((col) =>
        col.status === status ? { ...col, cards: [...col.cards, card] } : col,
      ),
    );
  };

  const moveCard = async (key: string, toStatus: IssueStatus) => {
    setDragging(null);
    let moved: IssueCard | undefined;
    let fromStatus: IssueStatus | undefined;
    board?.columns.forEach((col) => {
      const found = col.cards.find((c) => c.key === key);
      if (found) {
        moved = found;
        fromStatus = col.status;
      }
    });
    if (!moved || fromStatus === toStatus) return;

    mutateColumns((columns) =>
      columns.map((col) => {
        if (col.status === fromStatus) {
          return { ...col, cards: col.cards.filter((c) => c.key !== key) };
        }
        if (col.status === toStatus) {
          return { ...col, cards: [...col.cards, moved as IssueCard] };
        }
        return col;
      }),
    );

    try {
      await updateIssueStatus(key, toStatus);
    } catch {
      loadBoard(); // revert to server truth on failure
    }
  };

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h1 className="text-2xl font-semibold">Board</h1>
        <Button onClick={() => setCreateOpen(true)}>+ Create</Button>
      </div>

      <div className="flex flex-wrap gap-2">
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
        <FilterSelect label="Priority" value={priority} onChange={setPriority} options={[...PRIORITIES]} />
        <FilterSelect label="Type" value={type} onChange={setType} options={[...ISSUE_TYPES]} />
        <select
          aria-label="Group by"
          value={groupBy}
          onChange={(e) => setGroupBy(e.target.value as GroupBy)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
        >
          <option value="none">No grouping</option>
          <option value="priority">Group by priority</option>
          <option value="type">Group by type</option>
          <option value="assignee">Group by assignee</option>
        </select>
      </div>

      {error && (
        <p role="alert" className="text-sm text-red-600">
          {error}
        </p>
      )}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
        {board?.columns.map((col) => (
          <div
            key={col.status}
            data-testid={`column-${col.status}`}
            onMouseUp={() => dragging && moveCard(dragging, col.status)}
            className="flex flex-col gap-3 rounded-lg bg-slate-100 p-3"
          >
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold text-slate-700">
                {STATUS_LABELS[col.status]}
              </h2>
              <span
                data-testid={`count-${col.status}`}
                className="rounded-full bg-slate-200 px-2 text-xs text-slate-600"
              >
                {col.cards.length}
              </span>
            </div>

            {groupCards(col.cards, groupBy).map((group) => (
              <div key={group.label ?? "all"} className="flex flex-col gap-2">
                {group.label && (
                  <div className="text-xs uppercase tracking-wide text-slate-400">
                    {group.label}
                  </div>
                )}
                {group.cards.map((card) => (
                  <Card key={card.key} card={card} onDragStart={() => setDragging(card.key)} />
                ))}
              </div>
            ))}

            <InlineAdd
              onAdd={async (title) => {
                if (!projectKey) return;
                const issue = await createIssue({
                  projectKey,
                  title,
                  type: "TASK",
                  priority: "MEDIUM",
                  status: col.status,
                });
                addCard(col.status, toCard(issue));
              }}
            />
          </div>
        ))}
      </div>

      <CreateIssueModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreate={async (input) => {
          if (!projectKey) return;
          const issue = await createIssue({ ...input, projectKey });
          addCard(issue.status, toCard(issue));
          setCreateOpen(false);
        }}
      />
    </section>
  );
}

function toCard(issue: {
  id: number;
  key: string;
  title: string;
  priority: Priority;
  type: IssueType;
  storyPoints: number | null;
  assigneeId: number | null;
  assigneeName: string | null;
  epicName: string | null;
}): IssueCard {
  return {
    id: issue.id,
    key: issue.key,
    title: issue.title,
    priority: issue.priority,
    type: issue.type,
    storyPoints: issue.storyPoints,
    assigneeId: issue.assigneeId,
    assigneeName: issue.assigneeName,
    epicName: issue.epicName,
  };
}

function groupCards(
  cards: IssueCard[],
  groupBy: GroupBy,
): { label: string | null; cards: IssueCard[] }[] {
  if (groupBy === "none") {
    return [{ label: null, cards }];
  }
  const keyOf = (c: IssueCard) =>
    groupBy === "priority"
      ? c.priority
      : groupBy === "type"
        ? c.type
        : c.assigneeName ?? "Unassigned";
  const groups = new Map<string, IssueCard[]>();
  for (const card of cards) {
    const key = keyOf(card);
    groups.set(key, [...(groups.get(key) ?? []), card]);
  }
  return [...groups.entries()].map(([label, groupCards]) => ({ label, cards: groupCards }));
}

function Card({ card, onDragStart }: { card: IssueCard; onDragStart: () => void }) {
  return (
    <div
      data-testid={`card-${card.key}`}
      draggable={false}
      onMouseDown={onDragStart}
      className="cursor-grab rounded-md border border-slate-200 bg-white p-3 shadow-sm"
    >
      <div className="flex items-center justify-between">
        <Badge tone="accent">{card.key}</Badge>
        <Badge tone={PRIORITY_TONE[card.priority]}>{card.priority}</Badge>
      </div>
      <p className="mt-2 text-sm font-medium text-slate-800">{card.title}</p>
      <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
        <span>{card.storyPoints != null ? `${card.storyPoints} pts` : ""}</span>
        {card.assigneeName ? <Avatar name={card.assigneeName} /> : null}
      </div>
    </div>
  );
}

function InlineAdd({ onAdd }: { onAdd: (title: string) => Promise<void> }) {
  const [open, setOpen] = useState(false);
  const [title, setTitle] = useState("");

  if (!open) {
    return (
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="rounded-md px-2 py-1 text-left text-sm text-slate-500 hover:bg-slate-200"
      >
        + Add issue
      </button>
    );
  }

  return (
    <form
      onSubmit={async (e) => {
        e.preventDefault();
        if (!title.trim()) return;
        await onAdd(title.trim());
        setTitle("");
        setOpen(false);
      }}
    >
      <input
        autoFocus
        aria-label="New issue title"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        className="w-full rounded-md border border-slate-300 px-2 py-1 text-sm"
      />
    </form>
  );
}

function FilterSelect({
  label,
  value,
  onChange,
  options,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: string[];
}) {
  return (
    <select
      aria-label={label}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      className="rounded-md border border-slate-300 px-3 py-2 text-sm"
    >
      <option value="">All {label.toLowerCase()}</option>
      {options.map((opt) => (
        <option key={opt} value={opt}>
          {opt}
        </option>
      ))}
    </select>
  );
}

function CreateIssueModal({
  open,
  onClose,
  onCreate,
}: {
  open: boolean;
  onClose: () => void;
  onCreate: (input: {
    title: string;
    type: IssueType;
    priority: Priority;
    status: IssueStatus;
  }) => Promise<void>;
}) {
  const [title, setTitle] = useState("");
  const [type, setType] = useState<IssueType>("STORY");
  const [priority, setPriority] = useState<Priority>("MEDIUM");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await onCreate({ title, type, priority, status: "TODO" });
      setTitle("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create issue");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="Create Issue">
      <form onSubmit={submit} className="flex flex-col gap-3 text-sm">
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Title</span>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Type</span>
          <select
            value={type}
            onChange={(e) => setType(e.target.value as IssueType)}
            className="rounded-md border border-slate-300 px-3 py-2"
          >
            {ISSUE_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Priority</span>
          <select
            value={priority}
            onChange={(e) => setPriority(e.target.value as Priority)}
            className="rounded-md border border-slate-300 px-3 py-2"
          >
            {PRIORITIES.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
        </label>

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
            Create issue
          </Button>
        </div>
      </form>
    </Modal>
  );
}
