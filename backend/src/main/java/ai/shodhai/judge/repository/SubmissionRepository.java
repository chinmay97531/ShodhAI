package ai.shodhai.judge.repository;

import ai.shodhai.judge.domain.Submission;
import ai.shodhai.judge.domain.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByContestId(String contestId);

    @Query("SELECT s FROM Submission s WHERE s.contest.id = :contestId AND s.user.username = :username ORDER BY s.createdAt DESC")
    List<Submission> findLatestForUser(@Param("contestId") String contestId, @Param("username") String username);

    Optional<Submission> findFirstById(UUID id);

    long countByContestIdAndUserUsernameAndStatus(String contestId, String username, SubmissionStatus status);
}
