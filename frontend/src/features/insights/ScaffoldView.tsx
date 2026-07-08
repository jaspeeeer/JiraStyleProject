"use client";

import { useEffect, useState } from "react";
import { getReports, getRoadmap, getTimeline } from "@/lib/api/insights";
import { listProjects } from "@/lib/api/projects";

export type ScaffoldKind = "roadmap" | "reports" | "timeline";

const LOADERS = {
  roadmap: getRoadmap,
  reports: getReports,
  timeline: getTimeline,
};

/**
 * Shared empty-state for the not-yet-built insight pages (roadmap / reports / timeline).
 * Fetches its stub endpoint (for the first project) to prove the wiring and show the message.
 * Takes a serializable `kind` (not a function) so it can be used from a Server Component page.
 */
export function ScaffoldView({
  title,
  description,
  kind,
}: {
  title: string;
  description: string;
  kind: ScaffoldKind;
}) {
  const [message, setMessage] = useState<string>("Coming soon");

  useEffect(() => {
    let active = true;
    listProjects()
      .then((projects) => {
        if (projects.length === 0) return undefined;
        return LOADERS[kind](projects[0].key);
      })
      .then((info) => {
        if (active && info) setMessage(info.message);
      })
      .catch(() => {
        /* keep default placeholder */
      });
    return () => {
      active = false;
    };
  }, [kind]);

  return (
    <section className="flex flex-col gap-2">
      <h1 className="text-2xl font-semibold text-slate-900">{title}</h1>
      <p className="max-w-prose text-sm text-slate-500">{description}</p>
      <div
        data-testid="scaffold-message"
        className="mt-6 rounded-lg border border-dashed border-slate-300 bg-white p-10 text-center text-sm text-slate-400"
      >
        {message}
      </div>
    </section>
  );
}
