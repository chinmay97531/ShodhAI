package ai.shodhai.judge.repository;

import ai.shodhai.judge.domain.ContestUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContestUserRepository extends JpaRepository<ContestUser, Long> {
    Optional<ContestUser> findByUsernameIgnoreCase(String username);
}
