import clsx from "clsx";

const statusStyles: Record<string, string> = {
  Accepted: "bg-emerald-500/20 text-emerald-300 border-emerald-500/40",
  Running: "bg-sky-500/20 text-sky-200 border-sky-500/40",
  Pending: "bg-amber-500/20 text-amber-200 border-amber-500/40",
  Queued: "bg-amber-500/20 text-amber-200 border-amber-500/40",
  Rejected: "bg-rose-500/20 text-rose-200 border-rose-500/40",
  Failed: "bg-rose-500/20 text-rose-200 border-rose-500/40"
};

export interface SubmissionStatusBadgeProps {
  status: string;
}

export function SubmissionStatusBadge({ status }: SubmissionStatusBadgeProps) {
  const baseStyles = "inline-flex items-center rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-wide";
  const variant = statusStyles[status] ?? "bg-slate-800 text-slate-200 border-slate-700";
  return <span className={clsx(baseStyles, variant)}>{status}</span>;
}
