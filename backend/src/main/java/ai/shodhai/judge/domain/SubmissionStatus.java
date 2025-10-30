package ai.shodhai.judge.domain;

public enum SubmissionStatus {
    QUEUED,
    RUNNING,
    ACCEPTED,
    WRONG_ANSWER,
    RUNTIME_ERROR,
    COMPILE_ERROR,
    SYSTEM_ERROR
}
