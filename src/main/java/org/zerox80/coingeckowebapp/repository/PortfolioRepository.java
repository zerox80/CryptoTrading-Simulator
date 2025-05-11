package org.zerox80.coingeckowebapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerox80.coingeckowebapp.model.Portfolio;
import org.zerox80.coingeckowebapp.model.User;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUser(User user);
    Optional<Portfolio> findByUserId(Long userId);

    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.entries WHERE p.user = :user")
    Optional<Portfolio> findByUserWithEntries(@Param("user") User user);
} 