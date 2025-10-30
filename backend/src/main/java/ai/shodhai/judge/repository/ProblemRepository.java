package ai.shodhai.judge.repository;

import ai.shodhai.judge.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, String> {
    List<Problem> findByContestIdOrderByIdAsc(String contestId);
}
