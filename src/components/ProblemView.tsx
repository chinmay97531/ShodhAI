import type { Problem } from "@lib/api";

interface ProblemViewProps {
  problem: Problem | null;
  loading: boolean;
  error?: string | null;
}

export function ProblemView({ problem, loading, error }: ProblemViewProps) {
  if (loading) {
    return (
      <div className="flex h-full flex-col rounded-xl border border-slate-800 bg-slate-900/60 p-6">
        <div className="h-6 w-32 animate-pulse rounded bg-slate-700" />
        <div className="mt-4 space-y-2">
          <div className="h-4 w-full animate-pulse rounded bg-slate-800" />
          <div className="h-4 w-5/6 animate-pulse rounded bg-slate-800" />
          <div className="h-4 w-3/4 animate-pulse rounded bg-slate-800" />
          <div className="h-4 w-2/3 animate-pulse rounded bg-slate-800" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-full flex-col rounded-xl border border-rose-700/60 bg-rose-950/50 p-6 text-rose-200">
        <h2 className="text-lg font-semibold">Problem failed to load</h2>
        <p className="mt-2 text-sm">{error}</p>
      </div>
    );
  }

  if (!problem) {
    return (
      <div className="flex h-full flex-col items-center justify-center rounded-xl border border-slate-800 bg-slate-900/60 p-6 text-center text-slate-300">
        <p>Select a problem to get started.</p>
      </div>
    );
  }

  return (
    <article className="h-full overflow-y-auto rounded-xl border border-slate-800 bg-slate-900/70 p-6">
      <header className="mb-4 border-b border-slate-800 pb-4">
        <h2 className="text-2xl font-bold text-white">{problem.title}</h2>
        {problem.difficulty ? (
          <span className="mt-1 inline-block rounded-full bg-slate-800 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-slate-300">
            {problem.difficulty}
          </span>
        ) : null}
      </header>
      <div className="space-y-4 text-sm leading-relaxed text-slate-200">
        {(() => {
          const paragraphs = problem.statement
            .split(/\n\s*\n/)
            .map((paragraph) => paragraph.trim())
            .filter((paragraph) => paragraph.length > 0);

          if (paragraphs.length === 0) {
            return <p className="whitespace-pre-line">{problem.statement}</p>;
          }

          return paragraphs.map((paragraph, index) => (
            <p key={index} className="whitespace-pre-line">
              {paragraph}
            </p>
          ));
        })()}
      </div>
    </article>
  );
}
