package ai.shodhai.judge.controller;

import ai.shodhai.judge.dto.ContestResponse;
import ai.shodhai.judge.dto.LeaderboardEntryResponse;
import ai.shodhai.judge.dto.ProblemResponse;
import ai.shodhai.judge.service.ContestService;
import ai.shodhai.judge.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;
    private final LeaderboardService leaderboardService;

    public ContestController(ContestService contestService, LeaderboardService leaderboardService) {
        this.contestService = contestService;
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/{contestId}")
    public ResponseEntity<ContestResponse> getContest(@PathVariable String contestId) {
        return ResponseEntity.ok(contestService.getContest(contestId));
    }

    @GetMapping("/{contestId}/problems")
    public ResponseEntity<List<ProblemResponse>> getProblems(@PathVariable String contestId) {
        return ResponseEntity.ok(contestService.getProblems(contestId));
    }

    @GetMapping("/{contestId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(@PathVariable String contestId) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(contestId));
    }
}
