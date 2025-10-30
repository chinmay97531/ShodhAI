"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

export default function JoinPage() {
  const router = useRouter();
  const [contestId, setContestId] = useState("");
  const [username, setUsername] = useState("");
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!contestId.trim() || !username.trim()) {
      setError("Contest ID and username are required");
      return;
    }

    setError(null);
    const target = `/contest/${encodeURIComponent(contestId.trim())}?username=${encodeURIComponent(username.trim())}`;
    router.push(target);
  };

  return (
    <main className="mx-auto flex w-full max-w-lg flex-1 flex-col justify-center">
      <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-8 shadow-xl shadow-slate-950/60">
        <h1 className="text-3xl font-bold tracking-tight text-white">Join a Contest</h1>
        <p className="mt-2 text-sm text-slate-300">
          Enter the contest identifier shared with you along with your display name.
        </p>
        <form onSubmit={handleSubmit} className="mt-6 space-y-5">
          <div>
            <label htmlFor="contestId" className="block text-sm font-medium text-slate-200">
              Contest ID
            </label>
            <input
              id="contestId"
              name="contestId"
              value={contestId}
              onChange={(event) => setContestId(event.target.value)}
              placeholder="e.g. winter-open"
              className="mt-1 w-full rounded-lg border border-slate-700 px-4 py-2 text-base focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-600/60"
            />
          </div>
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-slate-200">
              Username
            </label>
            <input
              id="username"
              name="username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="e.g. Ada Lovelace"
              className="mt-1 w-full rounded-lg border border-slate-700 px-4 py-2 text-base focus:border-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-600/60"
            />
          </div>
          {error ? <p className="text-sm text-rose-400">{error}</p> : null}
          <button
            type="submit"
            className="w-full rounded-lg bg-sky-600 px-4 py-2 text-base font-semibold text-white shadow-lg shadow-sky-600/30 transition hover:bg-sky-500 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-slate-950"
          >
            Join Contest
          </button>
        </form>
      </div>
    </main>
  );
}
