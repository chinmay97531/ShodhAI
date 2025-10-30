package ai.shodhai.judge.repository;

import ai.shodhai.judge.domain.Contest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestRepository extends JpaRepository<Contest, String> {
}
