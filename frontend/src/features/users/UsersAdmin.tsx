"use client";

import { useEffect, useMemo, useState } from "react";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { Table, type Column } from "@/components/ui/Table";
import { ApiError } from "@/lib/api/client";
import { listProjects, type Project } from "@/lib/api/projects";
import {
  exportUsersCsv,
  inviteUser,
  listUsers,
  resendInvite,
  ROLES,
  STATUSES,
  type PageResponse,
  type Role,
  type User,
  type UserStatus,
} from "@/lib/api/users";

const PAGE_SIZE = 10;

const STATUS_TONE: Record<UserStatus, "success" | "warning" | "neutral"> = {
  ACTIVE: "success",
  INVITED: "warning",
  INACTIVE: "neutral",
};

export function UsersAdmin() {
  const [role, setRole] = useState("");
  const [status, setStatus] = useState("");
  const [projectId, setProjectId] = useState("");
  const [q, setQ] = useState("");
  const [page, setPage] = useState(0);

  const [data, setData] = useState<PageResponse<User> | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [inviteOpen, setInviteOpen] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    listProjects().then(setProjects).catch(() => setProjects([]));
  }, []);

  useEffect(() => {
    let active = true;
    setError(null);
    listUsers({
      role: role || undefined,
      status: status || undefined,
      projectId: projectId ? Number(projectId) : undefined,
      q: q || undefined,
      page,
      size: PAGE_SIZE,
    })
      .then((result) => {
        if (active) setData(result);
      })
      .catch((err: unknown) => {
        if (active) setError(err instanceof ApiError ? err.message : "Failed to load users");
      });
    return () => {
      active = false;
    };
  }, [role, status, projectId, q, page, reloadKey]);

  const refresh = () => setReloadKey((k) => k + 1);

  const onResend = async (user: User) => {
    setNotice(null);
    try {
      await resendInvite(user.id);
      setNotice(`Invite resent to ${user.email}`);
    } catch {
      setError("Failed to resend invite");
    }
  };

  const onExport = async () => {
    const blob = await exportUsersCsv({
      role: role || undefined,
      status: status || undefined,
      projectId: projectId ? Number(projectId) : undefined,
      q: q || undefined,
    });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = "users.csv";
    anchor.click();
    URL.revokeObjectURL(url);
  };

  const columns = useMemo<Column<User>[]>(
    () => [
      { header: "Name", cell: (u) => <span className="font-medium">{u.name}</span> },
      { header: "Email", cell: (u) => u.email },
      { header: "Role", cell: (u) => <Badge tone="accent">{u.role}</Badge> },
      {
        header: "Status",
        cell: (u) => <Badge tone={STATUS_TONE[u.status]}>{u.status}</Badge>,
      },
      {
        header: "Last active",
        cell: (u) =>
          u.lastActiveAt ? new Date(u.lastActiveAt).toLocaleDateString() : "—",
      },
      {
        header: "",
        cell: (u) =>
          u.status === "INVITED" ? (
            <Button variant="ghost" onClick={() => onResend(u)}>
              Resend
            </Button>
          ) : null,
      },
    ],
    // onResend is stable enough for this scaffold
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [],
  );

  return (
    <section className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Users &amp; Permissions</h1>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={onExport}>
            Export CSV
          </Button>
          <Button onClick={() => setInviteOpen(true)}>Invite User</Button>
        </div>
      </div>

      <div className="flex flex-wrap gap-2">
        <input
          aria-label="Search users"
          placeholder="Search name or email…"
          value={q}
          onChange={(e) => {
            setPage(0);
            setQ(e.target.value);
          }}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-accent"
        />
        <Select label="Role" value={role} onChange={setRole} options={[...ROLES]} onReset={() => setPage(0)} />
        <Select label="Status" value={status} onChange={setStatus} options={[...STATUSES]} onReset={() => setPage(0)} />
        <select
          aria-label="Project"
          value={projectId}
          onChange={(e) => {
            setPage(0);
            setProjectId(e.target.value);
          }}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm"
        >
          <option value="">All projects</option>
          {projects.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </select>
      </div>

      {notice && <p className="text-sm text-green-700">{notice}</p>}
      {error && (
        <p role="alert" className="text-sm text-red-600">
          {error}
        </p>
      )}

      <Table
        columns={columns}
        rows={data?.content ?? []}
        getRowKey={(u) => u.id}
        emptyMessage="No users match these filters"
      />

      {data && (
        <div className="flex items-center justify-between text-sm text-slate-500">
          <span>
            {data.totalElements} user{data.totalElements === 1 ? "" : "s"}
          </span>
          <div className="flex items-center gap-3">
            <Button
              variant="secondary"
              disabled={data.page <= 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              Prev
            </Button>
            <span>
              Page {data.page + 1} of {Math.max(1, data.totalPages)}
            </span>
            <Button
              variant="secondary"
              disabled={data.page >= data.totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </Button>
          </div>
        </div>
      )}

      <InviteModal
        open={inviteOpen}
        onClose={() => setInviteOpen(false)}
        onInvited={(email) => {
          setInviteOpen(false);
          setNotice(`Invitation sent to ${email}`);
          refresh();
        }}
      />
    </section>
  );
}

function Select({
  label,
  value,
  onChange,
  options,
  onReset,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: string[];
  onReset: () => void;
}) {
  return (
    <select
      aria-label={label}
      value={value}
      onChange={(e) => {
        onReset();
        onChange(e.target.value);
      }}
      className="rounded-md border border-slate-300 px-3 py-2 text-sm"
    >
      <option value="">All {label.toLowerCase()}s</option>
      {options.map((opt) => (
        <option key={opt} value={opt}>
          {opt}
        </option>
      ))}
    </select>
  );
}

function InviteModal({
  open,
  onClose,
  onInvited,
}: {
  open: boolean;
  onClose: () => void;
  onInvited: (email: string) => void;
}) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [role, setRole] = useState<Role>("DEVELOPER");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await inviteUser({ name, email, role });
      onInvited(email);
      setName("");
      setEmail("");
      setRole("DEVELOPER");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to invite user");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="Invite User">
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
          <span className="text-slate-600">Email</span>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="rounded-md border border-slate-300 px-3 py-2"
          />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-slate-600">Role</span>
          <select
            value={role}
            onChange={(e) => setRole(e.target.value as Role)}
            className="rounded-md border border-slate-300 px-3 py-2"
          >
            {ROLES.map((r) => (
              <option key={r} value={r}>
                {r}
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
            Send invite
          </Button>
        </div>
      </form>
    </Modal>
  );
}
