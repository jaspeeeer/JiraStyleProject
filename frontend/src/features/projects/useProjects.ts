import { useEffect, useState } from "react";
import { listProjects, type Project } from "@/lib/api/projects";

/**
 * Loads the project list and tracks the selected project key (defaulting to the first
 * project). Shared by the views that carry a project selector.
 */
export function useProjects() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [projectKey, setProjectKey] = useState("");

  useEffect(() => {
    let active = true;
    listProjects()
      .then((list) => {
        if (!active) return;
        setProjects(list);
        if (list.length > 0) {
          setProjectKey((current) => current || list[0].key);
        }
      })
      .catch(() => {
        if (active) setProjects([]);
      });
    return () => {
      active = false;
    };
  }, []);

  return { projects, projectKey, setProjectKey };
}
