package ai.shodhai.judge.service;

import ai.shodhai.judge.domain.Submission;
import ai.shodhai.judge.domain.SubmissionStatus;
import ai.shodhai.judge.dto.LeaderboardEntryResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaderboardService {

    private final SubmissionService submissionService;

    public LeaderboardService(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    public List<LeaderboardEntryResponse> getLeaderboard(String contestId) {
        List<Submission> submissions = submissionService.findByContest(contestId);
        Map<String, LeaderboardAccumulator> accumulatorMap = new HashMap<>();

        for (Submission submission : submissions) {
            String username = submission.getUser().getUsername();
            LeaderboardAccumulator accumulator = accumulatorMap.computeIfAbsent(username, key -> new LeaderboardAccumulator(username));
            if (submission.getStatus() == SubmissionStatus.ACCEPTED) {
                accumulator.score += 1.0;
                accumulator.time += submission.getExecutionTime() != null ? submission.getExecutionTime() : 0.0;
            }
        }

        List<LeaderboardAccumulator> sorted = new ArrayList<>(accumulatorMap.values());
        sorted.sort(Comparator
                .comparingDouble((LeaderboardAccumulator entry) -> entry.score).reversed()
                .thenComparingDouble(entry -> entry.time));

        List<LeaderboardEntryResponse> response = new ArrayList<>();
        int rank = 1;
        for (LeaderboardAccumulator entry : sorted) {
            response.add(new LeaderboardEntryResponse(entry.username, entry.score, entry.time, rank++));
        }
        return response;
    }

    private static class LeaderboardAccumulator {
        private final String username;
        private double score = 0.0;
        private double time = 0.0;

        private LeaderboardAccumulator(String username) {
            this.username = username;
        }
    }
}
