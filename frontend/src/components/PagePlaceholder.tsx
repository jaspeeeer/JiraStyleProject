/**
 * Uniform empty-state used by pages that are scaffolded but not yet built out.
 * Feature phases replace these with real screens.
 */
export function PagePlaceholder({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <section className="flex flex-col gap-2">
      <h1 className="text-2xl font-semibold text-slate-900">{title}</h1>
      <p className="max-w-prose text-sm text-slate-500">{description}</p>
      <div className="mt-6 rounded-lg border border-dashed border-slate-300 bg-white p-10 text-center text-sm text-slate-400">
        Coming soon
      </div>
    </section>
  );
}
