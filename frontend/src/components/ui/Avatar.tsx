function initials(name: string): string {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? "")
    .join("");
}

export function Avatar({ name }: { name: string }) {
  return (
    <span
      title={name}
      className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-sidebar text-xs font-semibold text-sidebar-foreground"
    >
      {initials(name)}
    </span>
  );
}
