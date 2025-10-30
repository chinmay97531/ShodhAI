package ai.shodhai.judge.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmissionRequest(
        @NotBlank(message = "contestId is required") String contestId,
        @NotBlank(message = "problemId is required") String problemId,
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "language is required") String language,
        @NotBlank(message = "code is required") String code
) {
}
