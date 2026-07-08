"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { me } from "@/lib/api/auth";

/**
 * Client-side route guard for the authenticated app shell. Calls /auth/me on mount; on failure
 * (401 or network) it redirects to /login. Cookies are httpOnly, so the client can't read the
 * token directly — it asks the server who it is.
 */
export function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    let active = true;
    me()
      .then(() => {
        if (active) setChecked(true);
      })
      .catch(() => {
        router.replace("/login");
      });
    return () => {
      active = false;
    };
  }, [router]);

  if (!checked) {
    return <div className="p-8 text-sm text-slate-500">Loading…</div>;
  }

  return <>{children}</>;
}
