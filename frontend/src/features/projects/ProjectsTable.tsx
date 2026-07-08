"use client";

import { useEffect, useState } from "react";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { Table, type Column } from "@/components/ui/Table";
import { ApiError } from "@/lib/api/client";
import { createProject, listProjects, type Project } from "@/lib/api/projects";

const COLUMNS: Column<Project>[] = [
  { header: "Key", cell: (p) => <Badge tone="accent">{p.key}</Badge> },
  { header: "Name", cell: (p) => <span className="font-medium">{p.name}</span> },
  { header: "Description", cell: (p) => p.description ?? "—" },
  { header: "Issues", cell: (p) => p.issueCounter },
];

type State =
  | { status: "loading" }
  | { status: "error"; message: string }
  | { status: "ready"; projects: Project[] };

export function ProjectsTable() {
  const [state, setState] = useState<State>({ status: "loading" });
  const [createOpen, setCreateOpen] = useState(false);

  useEffect(() => {
    let active = true;
    listProjects()
      .then((projects) => {
        if (active) setState({ status: "ready", projects });
      })
      .catch((error: unknown) => {
        const message =
          error instanceof ApiError ? error.message : "Failed to load projects";
        if (active) setState({ status: "error", message });
      });
    return () => {
      active = false;
    };
  }, []);

  const onCreated = (project: Project) => {
    setState((prev) =>
      prev.status === "ready"
        ? { status: "ready", projects: [...prev.projects, project] }
        : { status: "ready", projects: [project] },
    );
    setCreateOpen(false);
  };

  return (
    <section className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Projects</h1>
        <Button onClick={() => setCreateOpen(true)}>New Project</Button>
      </div>

      {state.status === "loading" && (
        <p className="text-sm text-slate-500">Loading projects…</p>
      )}

      {state.status === "error" && (
        <p role="alert" className="text-sm text-red-600">
          {state.message}
        </p>
      )}

      {state.status === "ready" && (
        <Table
          columns={COLUMNS}
          rows={state.projects}
          getRowKey={(p) => p.id}
          emptyMessage="No projects yet"
        />
      )}

      <CreateProjectModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={onCreated}
      />
    </section>
  );
}

function CreateProjectModal({
  open,
  onClose,
  onCreated,
}: {
  open: boolean;
  onClose: () => void;
  onCreated: (project: Project) => void;
}) {
  const [key, setKey] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const project = await createProject({
        key: key.trim(),
        name: name.trim(),
        description: description.trim() || undefined,
      });
      onCreated(project);
      setKey("");
      setName("");
      setDescription("");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create project");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="New Project">
      <form onSubmit={submit} className="flex flex-col gap-3 text-sm">
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Key</span>
          <input
            value={key}
            onChange={(e) => setKey(e.target.value.toUpperCase())}
            placeholder="PROJ"
            className="rounded-md border border-slate-300 px-3 py-2 uppercase"
          />
          <span className="text-xs text-slate-400">
            2–10 uppercase letters/digits; used as the issue-key prefix.
          </span>
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Name</span>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Description</span>
          <input
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2"
          />
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
            Create project
          </Button>
        </div>
      </form>
    </Modal>
  );
}
