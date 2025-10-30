const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

type FetchOptions = RequestInit & {
  parseJson?: boolean;
};

type QueryValue = string | number | boolean | null | undefined;

function buildQueryString(params: Record<string, QueryValue>): string {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") {
      return;
    }
    searchParams.append(key, String(value));
  });

  const queryString = searchParams.toString();
  return queryString ? `?${queryString}` : "";
}

function withQuery(path: string, params: Record<string, QueryValue>): string {
  return `${path}${buildQueryString(params)}`;
}

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

export async function getProblems(
  contestId: string,
  params: { username?: string } = {}
): Promise<Problem[]> {
  const path = withQuery(`/api/contests/${encodeURIComponent(contestId)}/problems`, params);
  return fetchJson<Problem[]>(path);
}

export async function getContest(
  contestId: string,
  params: { username?: string } = {}
): Promise<ContestDetails> {
  const path = withQuery(`/api/contests/${encodeURIComponent(contestId)}`, params);
  return fetchJson<ContestDetails>(path);
}

export async function getLeaderboard(
  contestId: string,
  params: { username?: string } = {}
): Promise<LeaderboardEntry[]> {
  const path = withQuery(`/api/contests/${encodeURIComponent(contestId)}/leaderboard`, params);
  return fetchJson<LeaderboardEntry[]>(path);
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
  return fetchJson<SubmissionStatus>(`/api/submissions/${encodeURIComponent(submissionId)}`);
}
