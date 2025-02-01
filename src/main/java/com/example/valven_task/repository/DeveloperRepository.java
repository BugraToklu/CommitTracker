package com.example.valven_task.repository;

import com.example.valven_task.model.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {
     Optional<Developer> findByEmail(String email);

}
