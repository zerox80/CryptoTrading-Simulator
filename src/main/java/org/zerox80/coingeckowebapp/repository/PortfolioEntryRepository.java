package org.zerox80.coingeckowebapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerox80.coingeckowebapp.model.Portfolio;
import org.zerox80.coingeckowebapp.model.PortfolioEntry;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioEntryRepository extends JpaRepository<PortfolioEntry, Long> {
    List<PortfolioEntry> findByPortfolio(Portfolio portfolio);
    Optional<PortfolioEntry> findByPortfolioAndCryptocurrencyId(Portfolio portfolio, String cryptocurrencyId);
} 