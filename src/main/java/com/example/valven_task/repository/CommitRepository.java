package com.example.valven_task.repository;

import com.example.valven_task.model.Commit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {
    boolean existsBySha(String sha);
    List<Commit> findByDeveloper_Email(String email);
    Optional<Commit> findBySha(String sha);

    List<Commit> findByAuthorEmail(String email);

    boolean existsByShaAndProvider(String sha, String provider);

}
