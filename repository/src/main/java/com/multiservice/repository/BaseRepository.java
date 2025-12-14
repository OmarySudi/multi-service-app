package com.multiservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for shared repository functionality.
 * Add common query methods here that will be inherited by all repositories.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    // Add common repository methods here
    // Example: List<T> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
