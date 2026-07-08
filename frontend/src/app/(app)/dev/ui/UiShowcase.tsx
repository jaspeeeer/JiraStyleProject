"use client";

import { useState } from "react";
import { Avatar } from "@/components/ui/Avatar";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { Tabs } from "@/components/ui/Tabs";

/**
 * Dev-only showcase used to exercise the UI primitives in Playwright. Not linked in the nav.
 */
export function UiShowcase() {
  const [modalOpen, setModalOpen] = useState(false);
  const [tab, setTab] = useState("one");

  return (
    <div className="flex flex-col gap-8">
      <h1 className="text-2xl font-semibold">UI primitives</h1>

      <section className="flex flex-wrap items-center gap-3">
        <Button>Primary</Button>
        <Button variant="secondary">Secondary</Button>
        <Button variant="ghost">Ghost</Button>
      </section>

      <section className="flex flex-wrap items-center gap-3">
        <Badge>Neutral</Badge>
        <Badge tone="accent">Accent</Badge>
        <Badge tone="success">Done</Badge>
        <Badge tone="warning">In review</Badge>
        <Badge tone="danger">Blocked</Badge>
        <Avatar name="Ada Admin" />
      </section>

      <section className="flex flex-col gap-2">
        <Tabs
          tabs={[
            { id: "one", label: "Sign In" },
            { id: "two", label: "Create Account" },
          ]}
          active={tab}
          onChange={setTab}
        />
        <p data-testid="active-tab" className="text-sm text-slate-600">
          Active tab: {tab}
        </p>
      </section>

      <section>
        <Button onClick={() => setModalOpen(true)}>Open modal</Button>
        <Modal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          title="Example modal"
        >
          <p className="text-sm text-slate-600">Modal body content.</p>
        </Modal>
      </section>
    </div>
  );
}
