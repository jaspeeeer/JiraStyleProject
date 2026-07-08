"use client";

import { useEffect, useState } from "react";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Table, type Column } from "@/components/ui/Table";
import { ApiError } from "@/lib/api/client";
import { listProjects, type Project } from "@/lib/api/projects";

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

  return (
    <section className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Projects</h1>
        <Button disabled>New Project</Button>
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
    </section>
  );
}
