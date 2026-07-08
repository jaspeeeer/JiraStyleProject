import { IssueDetailView } from "@/features/issue/IssueDetailView";

export default async function IssueDetailPage({
  params,
}: {
  params: Promise<{ key: string }>;
}) {
  const { key } = await params;
  return <IssueDetailView issueKey={key} />;
}
