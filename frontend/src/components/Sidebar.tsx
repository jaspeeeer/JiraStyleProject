"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { logout } from "@/lib/api/auth";

type NavItem = { href: string; label: string };

const MAIN_NAV: NavItem[] = [
  { href: "/projects", label: "Projects" },
  { href: "/board", label: "Board" },
  { href: "/backlog", label: "Backlog" },
  { href: "/roadmap", label: "Roadmap" },
  { href: "/reports", label: "Reports" },
  { href: "/timeline", label: "Timeline" },
];

const ADMIN_NAV: NavItem[] = [
  { href: "/admin/users", label: "Users" },
  { href: "/admin/roles", label: "Roles" },
  { href: "/admin/settings", label: "Settings" },
];

export function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();

  const handleSignOut = async () => {
    try {
      await logout();
    } finally {
      router.replace("/login");
    }
  };

  const renderLink = ({ href, label }: NavItem) => {
    const active = pathname === href || pathname.startsWith(`${href}/`);
    return (
      <Link
        key={href}
        href={href}
        className={`block rounded-md px-3 py-2 text-sm transition-colors ${
          active
            ? "bg-sidebar-active text-white"
            : "text-sidebar-muted hover:bg-sidebar-active hover:text-sidebar-foreground"
        }`}
      >
        {label}
      </Link>
    );
  };

  return (
    <aside className="flex w-60 flex-col gap-6 bg-sidebar px-3 py-5 text-sidebar-foreground">
      <div className="px-3 text-lg font-semibold">
        In<span className="text-accent">Track</span>
      </div>

      <nav className="flex flex-col gap-1">{MAIN_NAV.map(renderLink)}</nav>

      <div className="mt-auto flex flex-col gap-1">
        <div className="px-3 pb-1 text-xs uppercase tracking-wide text-sidebar-muted">
          Admin
        </div>
        {ADMIN_NAV.map(renderLink)}

        <button
          type="button"
          onClick={handleSignOut}
          className="mt-2 block rounded-md px-3 py-2 text-left text-sm text-sidebar-muted transition-colors hover:bg-sidebar-active hover:text-sidebar-foreground"
        >
          Sign out
        </button>
      </div>
    </aside>
  );
}
