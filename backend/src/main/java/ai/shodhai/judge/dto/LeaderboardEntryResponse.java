package ai.shodhai.judge.dto;

public record LeaderboardEntryResponse(String username, double score, double time, int rank) {
}
