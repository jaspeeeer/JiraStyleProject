"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { Button } from "@/components/ui/Button";
import { Tabs } from "@/components/ui/Tabs";
import { ApiError } from "@/lib/api/client";
import { forgotPassword, login, register } from "@/lib/api/auth";

type Mode = "signin" | "register" | "forgot";

export default function LoginPage() {
  const router = useRouter();
  const [mode, setMode] = useState<Mode>("signin");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const run = async (action: () => Promise<unknown>, onSuccess: () => void) => {
    setSubmitting(true);
    setError(null);
    setNotice(null);
    try {
      await action();
      onSuccess();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Something went wrong");
    } finally {
      setSubmitting(false);
    }
  };

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (mode === "signin") {
      run(() => login(email, password), () => router.push("/board"));
    } else if (mode === "register") {
      run(() => register(name, email, password), () => router.push("/board"));
    } else {
      run(
        () => forgotPassword(email),
        () => setNotice("If that email exists, a reset link has been sent."),
      );
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm rounded-xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-center text-xl font-semibold">
          In<span className="text-accent">Track</span>
        </h1>

        {mode !== "forgot" && (
          <div className="mt-6">
            <Tabs
              tabs={[
                { id: "signin", label: "Sign In" },
                { id: "register", label: "Create Account" },
              ]}
              active={mode}
              onChange={(id) => {
                setMode(id as Mode);
                setError(null);
                setNotice(null);
              }}
            />
          </div>
        )}

        <form onSubmit={onSubmit} className="mt-6 flex flex-col gap-3">
          {mode === "register" && (
            <Field label="Name" type="text" value={name} onChange={setName} />
          )}
          <Field label="Email" type="email" value={email} onChange={setEmail} />
          {mode !== "forgot" && (
            <Field label="Password" type="password" value={password} onChange={setPassword} />
          )}

          {error && (
            <p role="alert" className="text-sm text-red-600">
              {error}
            </p>
          )}
          {notice && <p className="text-sm text-green-700">{notice}</p>}

          <Button type="submit" disabled={submitting} className="mt-2 w-full">
            {mode === "signin" && "Sign In"}
            {mode === "register" && "Create Account"}
            {mode === "forgot" && "Send reset link"}
          </Button>
        </form>

        {mode === "signin" && (
          <>
            <button
              type="button"
              onClick={() => {
                setMode("forgot");
                setError(null);
              }}
              className="mt-3 text-sm text-slate-500 hover:text-slate-700"
            >
              Forgot password?
            </button>

            <Button
              variant="secondary"
              className="mt-4 w-full"
              onClick={() => setNotice("SSO is not configured yet.")}
            >
              Continue with SSO
            </Button>

            <p className="mt-4 text-center text-sm text-slate-500">
              Need access?{" "}
              <a className="text-accent hover:underline" href="mailto:it-access@standard-insurance.com">
                Request access
              </a>
            </p>
          </>
        )}

        {mode === "forgot" && (
          <button
            type="button"
            onClick={() => setMode("signin")}
            className="mt-3 text-sm text-slate-500 hover:text-slate-700"
          >
            ← Back to sign in
          </button>
        )}
      </div>
    </div>
  );
}

function Field({
  label,
  type,
  value,
  onChange,
}: {
  label: string;
  type: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className="flex flex-col gap-1 text-sm">
      <span className="text-slate-600">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-accent"
      />
    </label>
  );
}
