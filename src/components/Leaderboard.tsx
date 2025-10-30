import type { LeaderboardEntry } from "@lib/api";

interface LeaderboardProps {
  entries: LeaderboardEntry[];
  loading: boolean;
  error?: string | null;
}

function formatTime(totalSeconds: number) {
  const clamped = Math.max(0, totalSeconds);
  const minutes = Math.floor(clamped / 60);
  const seconds = Math.floor(clamped % 60);
  return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
}

export function Leaderboard({ entries, loading, error }: LeaderboardProps) {
  if (loading) {
    return (
      <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4">
        <div className="h-5 w-32 animate-pulse rounded bg-slate-700" />
        <div className="mt-4 space-y-2">
          {Array.from({ length: 5 }).map((_, index) => (
            <div key={index} className="h-10 w-full animate-pulse rounded bg-slate-800" />
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-xl border border-rose-700/60 bg-rose-950/40 p-4 text-rose-200">
        <h3 className="text-lg font-semibold">Leaderboard unavailable</h3>
        <p className="mt-2 text-sm">{error}</p>
      </div>
    );
  }

  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900/60">
      <header className="border-b border-slate-800 p-4">
        <h3 className="text-lg font-semibold text-white">Leaderboard</h3>
      </header>
      <div className="max-h-[480px] overflow-y-auto">
        <table className="min-w-full divide-y divide-slate-800 text-sm">
          <thead className="bg-slate-900/80 text-left text-xs uppercase tracking-wide text-slate-400">
            <tr>
              <th className="px-4 py-3">Rank</th>
              <th className="px-4 py-3">Participant</th>
              <th className="px-4 py-3 text-right">Score</th>
              <th className="px-4 py-3 text-right">Time</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {entries.map((entry) => (
              <tr key={entry.username} className="hover:bg-slate-800/40">
                <td className="px-4 py-3 font-semibold text-slate-200">{entry.rank}</td>
                <td className="px-4 py-3 text-slate-100">{entry.username}</td>
                <td className="px-4 py-3 text-right font-mono text-slate-200">{entry.score}</td>
                <td className="px-4 py-3 text-right font-mono text-slate-300">{formatTime(entry.time)}</td>
              </tr>
            ))}
            {entries.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-4 py-8 text-center text-slate-400">
                  No submissions yet. Be the first to solve the problem!
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
