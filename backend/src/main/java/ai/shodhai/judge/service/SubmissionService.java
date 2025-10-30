package ai.shodhai.judge.service;

import ai.shodhai.judge.domain.Contest;
import ai.shodhai.judge.domain.ContestUser;
import ai.shodhai.judge.domain.Problem;
import ai.shodhai.judge.domain.Submission;
import ai.shodhai.judge.domain.SubmissionStatus;
import ai.shodhai.judge.dto.SubmissionRequest;
import ai.shodhai.judge.repository.ContestRepository;
import ai.shodhai.judge.repository.ContestUserRepository;
import ai.shodhai.judge.repository.ProblemRepository;
import ai.shodhai.judge.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubmissionService {

    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final ContestUserRepository contestUserRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionService(ContestRepository contestRepository,
                             ProblemRepository problemRepository,
                             ContestUserRepository contestUserRepository,
                             SubmissionRepository submissionRepository) {
        this.contestRepository = contestRepository;
        this.problemRepository = problemRepository;
        this.contestUserRepository = contestUserRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional
    public Submission createSubmission(SubmissionRequest request) {
        Contest contest = contestRepository.findById(request.contestId())
                .orElseThrow(() -> new IllegalArgumentException("Contest not found"));
        Problem problem = problemRepository.findById(request.problemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

        if (!problem.getContest().getId().equals(contest.getId())) {
            throw new IllegalArgumentException("Problem does not belong to this contest");
        }

        ContestUser user = contestUserRepository.findByUsernameIgnoreCase(request.username())
                .orElseGet(() -> contestUserRepository.save(new ContestUser(request.username(), request.username())));

        Submission submission = new Submission();
        submission.setContest(contest);
        submission.setProblem(problem);
        submission.setUser(user);
        submission.setLanguage(request.language());
        submission.setSourceCode(request.code());
        submission.setStatus(SubmissionStatus.QUEUED);
        submission.setVerdict("Pending");
        submission.setScore(0.0);

        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public Submission getSubmission(UUID submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    }

    @Transactional
    public Submission save(Submission submission) {
        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<Submission> findByContest(String contestId) {
        return submissionRepository.findByContestId(contestId);
    }
}
