const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

type FetchOptions = RequestInit & {
  parseJson?: boolean;
};

export async function fetchJson<T>(input: string, init: FetchOptions = {}): Promise<T> {
  const { parseJson = true, headers, ...rest } = init;
  const response = await fetch(`${API_BASE_URL}${input}`, {
    ...rest,
    headers: {
      "Content-Type": "application/json",
      ...(headers ?? {})
    }
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  if (!parseJson) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export interface Problem {
  id: string;
  title: string;
  statement: string;
  difficulty?: string;
}

export interface LeaderboardEntry {
  username: string;
  score: number;
  time: number;
  rank: number;
}

export interface SubmissionResponse {
  submissionId: string;
}

export interface SubmissionStatus {
  status: string;
  verdict?: string;
  score?: number;
  time?: number;
}

export interface ContestDetails {
  contestId: string;
  name: string;
  description?: string;
}

export async function getProblems(contestId: string): Promise<Problem[]> {
  return fetchJson<Problem[]>(`/api/contests/${contestId}/problems`);
}

export async function getContest(contestId: string): Promise<ContestDetails> {
  return fetchJson<ContestDetails>(`/api/contests/${contestId}`);
}

export async function getLeaderboard(contestId: string): Promise<LeaderboardEntry[]> {
  return fetchJson<LeaderboardEntry[]>(`/api/contests/${contestId}/leaderboard`);
}

export async function submitSolution(
  payload: Record<string, unknown>
): Promise<SubmissionResponse> {
  return fetchJson<SubmissionResponse>("/api/submissions", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function getSubmissionStatus(submissionId: string): Promise<SubmissionStatus> {
  return fetchJson<SubmissionStatus>(`/api/submissions/${submissionId}`);
}
