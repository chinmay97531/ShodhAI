package ai.shodhai.judge.controller;

import ai.shodhai.judge.domain.Submission;
import ai.shodhai.judge.domain.SubmissionStatus;
import ai.shodhai.judge.dto.SubmissionRequest;
import ai.shodhai.judge.dto.SubmissionResponse;
import ai.shodhai.judge.dto.SubmissionStatusResponse;
import ai.shodhai.judge.service.SubmissionQueue;
import ai.shodhai.judge.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionQueue submissionQueue;

    public SubmissionController(SubmissionService submissionService, SubmissionQueue submissionQueue) {
        this.submissionService = submissionService;
        this.submissionQueue = submissionQueue;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> createSubmission(@RequestBody @Valid SubmissionRequest request) {
        Submission submission = submissionService.createSubmission(request);
        submissionQueue.enqueue(submission.getId());
        return ResponseEntity.accepted(new SubmissionResponse(submission.getId().toString()));
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionStatusResponse> getSubmission(@PathVariable String submissionId) {
        Submission submission = submissionService.getSubmission(UUID.fromString(submissionId));
        return ResponseEntity.ok(new SubmissionStatusResponse(
                normalizeStatus(submission.getStatus()),
                submission.getVerdict(),
                submission.getScore(),
                submission.getExecutionTime()
        ));
    }

    private String normalizeStatus(SubmissionStatus status) {
        String raw = status.name().toLowerCase().replace('_', ' ');
        String[] parts = raw.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(parts[i].charAt(0)))
                    .append(parts[i].substring(1));
        }
        return builder.toString();
    }
}
