package ai.shodhai.judge.service;

import ai.shodhai.judge.domain.Submission;
import ai.shodhai.judge.domain.SubmissionStatus;
import ai.shodhai.judge.service.JudgeExecutionService.ExecutionSummary;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SubmissionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionProcessingService.class);

    private final SubmissionQueue submissionQueue;
    private final SubmissionService submissionService;
    private final JudgeExecutionService judgeExecutionService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public SubmissionProcessingService(SubmissionQueue submissionQueue,
                                       SubmissionService submissionService,
                                       JudgeExecutionService judgeExecutionService) {
        this.submissionQueue = submissionQueue;
        this.submissionService = submissionService;
        this.judgeExecutionService = judgeExecutionService;
    }

    @PostConstruct
    public void start() {
        running.set(true);
        executor.submit(this::pollQueue);
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        executor.shutdownNow();
    }

    private void pollQueue() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                UUID submissionId = submissionQueue.take();
                processSubmission(submissionId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Failed to process submission", e);
            }
        }
    }

    @Transactional
    public void processSubmission(UUID submissionId) {
        Submission submission = submissionService.getSubmission(submissionId);
        submission.setStatus(SubmissionStatus.RUNNING);
        submission.setVerdict("Running");
        submissionService.save(submission);

        ExecutionSummary result = judgeExecutionService.judgeSubmission(submission);

        if (result.accepted()) {
            submission.setStatus(SubmissionStatus.ACCEPTED);
            submission.setScore(result.score());
        } else {
            submission.setStatus(resolveFailureStatus(result.verdict()));
            submission.setScore(0.0);
        }
        submission.setExecutionTime(result.timeSeconds());
        submission.setVerdict(result.verdict());
        if (result.message() != null) {
            submission.setVerdict(result.verdict() + ": " + result.message());
        }

        submissionService.save(submission);
    }

    private SubmissionStatus resolveFailureStatus(String verdict) {
        if (verdict == null) {
            return SubmissionStatus.WRONG_ANSWER;
        }
        return switch (verdict.toLowerCase()) {
            case "wrong answer" -> SubmissionStatus.WRONG_ANSWER;
            case "runtime error" -> SubmissionStatus.RUNTIME_ERROR;
            case "compile error" -> SubmissionStatus.COMPILE_ERROR;
            case "time limit exceeded" -> SubmissionStatus.RUNTIME_ERROR;
            case "unsupported language" -> SubmissionStatus.COMPILE_ERROR;
            case "system error" -> SubmissionStatus.SYSTEM_ERROR;
            default -> SubmissionStatus.WRONG_ANSWER;
        };
    }
}
