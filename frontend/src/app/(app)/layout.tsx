import { AuthGuard } from "@/components/AuthGuard";
import { Sidebar } from "@/components/Sidebar";

/**
 * Authenticated app shell: guarded by {@link AuthGuard}, then a dark sidebar + main content area.
 */
export default function AppLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <AuthGuard>
      <div className="flex min-h-screen">
        <Sidebar />
        <main className="flex-1 px-8 py-6">{children}</main>
      </div>
    </AuthGuard>
  );
}
