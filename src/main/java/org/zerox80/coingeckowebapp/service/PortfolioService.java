package org.zerox80.coingeckowebapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerox80.coingeckowebapp.model.Portfolio;
import org.zerox80.coingeckowebapp.model.User;
import org.zerox80.coingeckowebapp.repository.PortfolioRepository;
import org.zerox80.coingeckowebapp.repository.UserRepository;
import org.zerox80.coingeckowebapp.repository.PortfolioEntryRepository;
import org.zerox80.coingeckowebapp.model.PortfolioEntry;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;
import java.util.List;
import java.util.Map;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import reactor.core.publisher.Mono;

@Service
public class PortfolioService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioService.class);

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final PortfolioEntryRepository portfolioEntryRepository;
    private final CryptoService cryptoService;

    @Value("${portfolio.initial.balance:1000000.00}")
    private BigDecimal initialBalance;

    @Value("${portfolio.initial.currency:EUR}")
    private String initialCurrency;

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    public PortfolioService(PortfolioRepository portfolioRepository, UserRepository userRepository, PortfolioEntryRepository portfolioEntryRepository, CryptoService cryptoService) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.portfolioEntryRepository = portfolioEntryRepository;
        this.cryptoService = cryptoService;
    }

    @Transactional
    public Portfolio createInitialPortfolio(User user) {
        if (portfolioRepository.findByUser(user).isPresent()) {
            logger.warn("Portfolio for user {} already exists. Skipping creation.", user.getUsername());
            return portfolioRepository.findByUser(user).get();
        }

        Portfolio portfolio = new Portfolio(user, initialBalance, initialCurrency);
        user.setPortfolio(portfolio);
        
        logger.info("Creating initial portfolio for user {} with balance {} {}", user.getUsername(), initialBalance, initialCurrency);
        return portfolioRepository.save(portfolio);
    }

    @Transactional(readOnly = true)
    public Portfolio getPortfolioForUser(User user) {
        return portfolioRepository.findByUserWithEntries(user)
                .orElseThrow(() -> new RuntimeException("Portfolio not found for user: " + user.getUsername()));
    }

    @Transactional(readOnly = true)
    public Portfolio getPortfolioByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return getPortfolioForUser(user);
    }

    @Transactional
    public Mono<Void> buyCryptocurrency(User user, String cryptocurrencyId, String cryptocurrencyName, String cryptocurrencySymbol, double currentPriceDouble, BigDecimal amountToBuy) {
        Portfolio portfolio = getPortfolioForUser(user);

        String normalizedCryptocurrencyId = cryptocurrencyId.toLowerCase();

        return cryptoService.getCurrentPrices(List.of(normalizedCryptocurrencyId), portfolio.getCurrency().toLowerCase())
            .flatMap(pricesMap -> {
                if (pricesMap == null || !pricesMap.containsKey(normalizedCryptocurrencyId) || !pricesMap.get(normalizedCryptocurrencyId).containsKey(portfolio.getCurrency().toLowerCase())) {
                    return Mono.error(new RuntimeException("Could not fetch price for cryptocurrency: " + normalizedCryptocurrencyId + " in currency: " + portfolio.getCurrency()));
                }

                Double priceDouble = pricesMap.get(normalizedCryptocurrencyId).get(portfolio.getCurrency().toLowerCase());
                if (priceDouble == null) {
                    return Mono.error(new RuntimeException("Price for cryptocurrency: " + normalizedCryptocurrencyId + " was null."));
                }
                BigDecimal currentPrice = BigDecimal.valueOf(priceDouble);
                
                BigDecimal totalCost = amountToBuy.multiply(currentPrice).setScale(portfolio.getBalance().scale(), RoundingMode.HALF_UP);

                if (portfolio.getBalance().compareTo(totalCost) < 0) {
                    return Mono.error(new InsufficientFundsException("Insufficient funds. You need " + totalCost + " " + portfolio.getCurrency() +
                                                       " but only have " + portfolio.getBalance() + " " + portfolio.getCurrency() + "."));
                }

                portfolio.setBalance(portfolio.getBalance().subtract(totalCost));

                Optional<PortfolioEntry> existingEntryOpt = portfolioEntryRepository.findByPortfolioAndCryptocurrencyId(portfolio, normalizedCryptocurrencyId);

                if (existingEntryOpt.isPresent()) {
                    PortfolioEntry existingEntry = existingEntryOpt.get();
                    BigDecimal oldAmount = existingEntry.getAmount();
                    BigDecimal oldAvgPrice = existingEntry.getAveragePurchasePrice();
                    
                    BigDecimal newAmount = oldAmount.add(amountToBuy);
                    BigDecimal newAvgPrice = (oldAmount.multiply(oldAvgPrice))
                                             .add(amountToBuy.multiply(currentPrice))
                                             .divide(newAmount, 4, RoundingMode.HALF_UP);

                    existingEntry.setAmount(newAmount);
                    existingEntry.setAveragePurchasePrice(newAvgPrice);
                    
                    PortfolioEntry savedManagedEntry = portfolioEntryRepository.save(existingEntry);

                    final Long entryId = savedManagedEntry.getId();
                    portfolio.getEntries().removeIf(e -> e.getId() != null && e.getId().equals(entryId));
                    portfolio.getEntries().add(savedManagedEntry);

                } else {
                    PortfolioEntry newEntry = new PortfolioEntry(portfolio, normalizedCryptocurrencyId, cryptocurrencySymbol, cryptocurrencyName, amountToBuy, currentPrice);
                    portfolio.getEntries().add(newEntry);
                }

                portfolioRepository.save(portfolio);
                logger.info("Successfully processed purchase for user {}: {} of {} for {} {}", 
                    user.getUsername(), amountToBuy, normalizedCryptocurrencyId, totalCost, portfolio.getCurrency());
                return Mono.empty();
            });
    }
} 