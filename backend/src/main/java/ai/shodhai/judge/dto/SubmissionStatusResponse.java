package ai.shodhai.judge.dto;

public record SubmissionStatusResponse(String status, String verdict, Double score, Double time) {
}
