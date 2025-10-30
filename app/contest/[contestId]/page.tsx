"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams, useSearchParams } from "next/navigation";
import {
  ContestDetails,
  LeaderboardEntry,
  Problem,
  SubmissionStatus,
  getContest,
  getLeaderboard,
  getProblems,
  getSubmissionStatus,
  submitSolution
} from "@lib/api";
import { ProblemView } from "@components/ProblemView";
import { CodeEditor } from "@components/CodeEditor";
import { Leaderboard } from "@components/Leaderboard";
import { SubmissionStatusBadge } from "@components/SubmissionStatusBadge";
import { usePolling } from "@hooks/usePolling";

const DEFAULT_LANGUAGE = "python";
const LIVE_POLLING_INTERVAL = 20000;
const SUBMISSION_POLLING_INTERVAL = 2500;

const ACTIVE_STATUSES = new Set(["Pending", "Running", "Queued"]);

export default function ContestPage() {
  const params = useParams<{ contestId: string }>();
  const searchParams = useSearchParams();
  const rawContestId = params.contestId;
  const contestId = useMemo(() => {
    if (!rawContestId) {
      return "";
    }

    try {
      return decodeURIComponent(rawContestId);
    } catch (error) {
      console.warn("Failed to decode contest id", error);
      return rawContestId;
    }
  }, [rawContestId]);
  const username = searchParams.get("username") ?? "Anonymous";

  const [contest, setContest] = useState<ContestDetails | null>(null);
  const [contestError, setContestError] = useState<string | null>(null);
  const [contestLoading, setContestLoading] = useState(true);

  const [problems, setProblems] = useState<Problem[]>([]);
  const [problemsLoading, setProblemsLoading] = useState(true);
  const [problemsError, setProblemsError] = useState<string | null>(null);
  const [selectedProblemId, setSelectedProblemId] = useState<string | null>(null);

  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [leaderboardLoading, setLeaderboardLoading] = useState(true);
  const [leaderboardError, setLeaderboardError] = useState<string | null>(null);

  const [code, setCode] = useState<string>("# Write your solution here\n");
  const [language, setLanguage] = useState<string>(DEFAULT_LANGUAGE);

  const [submissionId, setSubmissionId] = useState<string | null>(null);
  const [submissionStatus, setSubmissionStatus] = useState<SubmissionStatus | null>(null);
  const [submissionError, setSubmissionError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isPollingSubmission, setIsPollingSubmission] = useState(false);

  const selectedProblem = useMemo(
    () => problems.find((problem) => problem.id === selectedProblemId) ?? null,
    [problems, selectedProblemId]
  );

  useEffect(() => {
    if (!contestId) {
      return;
    }

    let active = true;
    setContestLoading(true);
    setContestError(null);
    getContest(contestId, { username })
      .then((data) => {
        if (!active) return;
        setContest(data);
      })
      .catch((error) => {
        if (!active) return;
        setContestError(error.message);
      })
      .finally(() => {
        if (!active) return;
        setContestLoading(false);
      });

    return () => {
      active = false;
    };
  }, [contestId, username]);

  useEffect(() => {
    if (!contestId) {
      return;
    }

    let active = true;
    setProblemsLoading(true);
    setProblemsError(null);
    getProblems(contestId, { username })
      .then((data) => {
        if (!active) return;
        setProblems(data);
        setSelectedProblemId((current) => {
          if (current && data.some((problem) => problem.id === current)) {
            return current;
          }
          return data[0]?.id ?? null;
        });
      })
      .catch((error) => {
        if (!active) return;
        setProblemsError(error.message);
      })
      .finally(() => {
        if (!active) return;
        setProblemsLoading(false);
      });

    return () => {
      active = false;
    };
  }, [contestId, username]);

  const fetchLeaderboard = useCallback(async () => {
    try {
      if (!contestId) {
        return;
      }

      const data = await getLeaderboard(contestId, { username });
      setLeaderboard(data);
      setLeaderboardError(null);
    } catch (error) {
      console.error(error);
      setLeaderboardError(error instanceof Error ? error.message : "Failed to load leaderboard");
    } finally {
      setLeaderboardLoading(false);
    }
  }, [contestId, username]);

  useEffect(() => {
    setLeaderboardLoading(true);
    setLeaderboardError(null);
  }, [contestId, username]);

  usePolling(fetchLeaderboard, {
    interval: LIVE_POLLING_INTERVAL,
    enabled: true
  });

  const pollSubmissionStatus = useCallback(async () => {
    if (!submissionId) {
      return;
    }

    try {
      const status = await getSubmissionStatus(submissionId);
      setSubmissionStatus(status);
      if (!ACTIVE_STATUSES.has(status.status)) {
        setIsPollingSubmission(false);
        setSubmissionId(null);
      }
    } catch (error) {
      console.error(error);
      setSubmissionError(error instanceof Error ? error.message : "Failed to check submission status");
      setIsPollingSubmission(false);
    }
  }, [submissionId]);

  usePolling(pollSubmissionStatus, {
    interval: SUBMISSION_POLLING_INTERVAL,
    enabled: isPollingSubmission && Boolean(submissionId)
  });

  const handleSubmit = async () => {
    if (!selectedProblem) {
      setSubmissionError("Select a problem to submit.");
      return;
    }

    if (!code.trim()) {
      setSubmissionError("Code cannot be empty.");
      return;
    }

    setSubmissionError(null);
    setIsSubmitting(true);
    try {
      const payload = {
        contestId: contest?.contestId ?? contestId ?? rawContestId,
        problemId: selectedProblem.id,
        username,
        language,
        code
      };
      const { submissionId: newSubmissionId } = await submitSolution(payload);
      setSubmissionId(newSubmissionId);
      setSubmissionStatus({ status: "Pending" });
      setIsPollingSubmission(true);
    } catch (error) {
      console.error(error);
      setSubmissionError(error instanceof Error ? error.message : "Failed to submit solution");
    } finally {
      setIsSubmitting(false);
    }
  };

  const fallbackContestIdentifier = contest?.contestId ?? (contestId || rawContestId);
  const contestTitle = contest?.name ?? `Contest ${fallbackContestIdentifier}`;

  return (
    <main className="flex flex-1 flex-col space-y-6">
      <header className="space-y-2">
        <div className="flex flex-col gap-2 text-sm text-slate-300 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Contest</p>
            <h1 className="text-2xl font-bold text-white sm:text-3xl">
              {contestLoading ? "Loading contest..." : contestTitle}
            </h1>
            {contest?.description ? (
              <p className="mt-2 max-w-2xl text-sm text-slate-300">{contest.description}</p>
            ) : null}
            {contestError ? <p className="mt-2 text-sm text-rose-400">{contestError}</p> : null}
          </div>
          <div className="rounded-lg border border-slate-800 bg-slate-900/80 px-4 py-2 text-right">
            <p className="text-xs uppercase tracking-wide text-slate-400">Signed in as</p>
            <p className="font-semibold text-slate-100">{username}</p>
          </div>
        </div>
      </header>

      <section className="grid flex-1 grid-cols-1 gap-6 xl:grid-cols-[2fr_3fr_2fr]">
        <div className="flex flex-col gap-4">
          <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4">
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-xs uppercase tracking-wide text-slate-400">Problem</p>
                <h2 className="text-lg font-semibold text-white">{selectedProblem?.title ?? "Select a problem"}</h2>
              </div>
              <select
                className="rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100"
                value={selectedProblemId ?? ""}
                onChange={(event) => setSelectedProblemId(event.target.value)}
                disabled={problemsLoading || problems.length === 0}
              >
                <option value="" disabled>
                  {problemsLoading ? "Loading..." : "Choose a problem"}
                </option>
                {problems.map((problem) => (
                  <option key={problem.id} value={problem.id}>
                    {problem.title}
                  </option>
                ))}
              </select>
            </div>
            {problemsError ? <p className="mt-3 text-sm text-rose-400">{problemsError}</p> : null}
          </div>
          <div className="flex-1 overflow-hidden">
            <ProblemView problem={selectedProblem} loading={problemsLoading} error={problemsError} />
          </div>
        </div>

        <div className="flex flex-col gap-4">
          <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-800 bg-slate-900/60 px-4 py-3">
            <div className="flex items-center gap-3">
              <label className="text-sm font-medium text-slate-200" htmlFor="language">
                Language
              </label>
              <select
                id="language"
                className="rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100"
                value={language}
                onChange={(event) => setLanguage(event.target.value)}
              >
                <option value="python">Python</option>
                <option value="cpp">C++</option>
                <option value="java">Java</option>
                <option value="javascript">JavaScript</option>
              </select>
            </div>
            <div className="flex items-center gap-3">
              {submissionStatus ? <SubmissionStatusBadge status={submissionStatus.status} /> : null}
              <button
                type="button"
                onClick={handleSubmit}
                disabled={isSubmitting || isPollingSubmission || !selectedProblem}
                className="inline-flex items-center justify-center rounded-lg bg-sky-600 px-4 py-2 text-sm font-semibold text-white shadow-sky-600/30 transition hover:bg-sky-500 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isSubmitting ? "Submitting..." : isPollingSubmission ? "Evaluating..." : "Submit"}
              </button>
            </div>
          </div>
          <div className="h-[520px] flex-1 overflow-hidden">
            <CodeEditor value={code} language={language} onChange={setCode} />
          </div>
          {submissionError ? <p className="text-sm text-rose-400">{submissionError}</p> : null}
          {submissionStatus?.verdict ? (
            <div className="rounded-lg border border-slate-800 bg-slate-900/80 p-4 text-sm text-slate-200">
              <p className="font-semibold text-slate-100">Latest Verdict</p>
              <p className="mt-1 text-slate-200">{submissionStatus.verdict}</p>
              {typeof submissionStatus.score === "number" ? (
                <p className="mt-2 text-slate-300">Score: {submissionStatus.score}</p>
              ) : null}
              {typeof submissionStatus.time === "number" ? (
                <p className="text-slate-400">Time: {submissionStatus.time.toFixed(2)}s</p>
              ) : null}
            </div>
          ) : null}
        </div>

        <div className="flex flex-col">
          <Leaderboard entries={leaderboard} loading={leaderboardLoading} error={leaderboardError} />
        </div>
      </section>
    </main>
  );
}
