package ai.shodhai.judge.service;

import ai.shodhai.judge.domain.Contest;
import ai.shodhai.judge.domain.Problem;
import ai.shodhai.judge.dto.ContestResponse;
import ai.shodhai.judge.dto.ProblemResponse;
import ai.shodhai.judge.repository.ContestRepository;
import ai.shodhai.judge.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ContestService {

    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;

    public ContestService(ContestRepository contestRepository, ProblemRepository problemRepository) {
        this.contestRepository = contestRepository;
        this.problemRepository = problemRepository;
    }

    public ContestResponse getContest(String contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new IllegalArgumentException("Contest not found"));
        return new ContestResponse(contest.getId(), contest.getName(), contest.getDescription());
    }

    public List<ProblemResponse> getProblems(String contestId) {
        List<Problem> problems = problemRepository.findByContestIdOrderByIdAsc(contestId);
        return problems.stream()
                .map(problem -> new ProblemResponse(problem.getId(), problem.getTitle(), problem.getStatement(), problem.getDifficulty()))
                .collect(Collectors.toList());
    }
}
