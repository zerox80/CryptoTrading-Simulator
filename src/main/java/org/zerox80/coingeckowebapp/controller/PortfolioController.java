package org.zerox80.coingeckowebapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import org.zerox80.coingeckowebapp.model.Portfolio;
import org.zerox80.coingeckowebapp.model.PortfolioEntry;
import org.zerox80.coingeckowebapp.model.PortfolioEntryViewModel;
import org.zerox80.coingeckowebapp.service.PortfolioService;
import org.zerox80.coingeckowebapp.service.UserService;
import org.zerox80.coingeckowebapp.service.CryptoService;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Controller
@RequestMapping("/portfolio")
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);
    private final PortfolioService portfolioService;
    private final UserService userService;
    private final CryptoService cryptoService;

    public PortfolioController(PortfolioService portfolioService, UserService userService, CryptoService cryptoService) {
        this.portfolioService = portfolioService;
        this.userService = userService;
        this.cryptoService = cryptoService;
    }

    @GetMapping
    public Mono<String> viewPortfolio(@AuthenticationPrincipal UserDetails userDetails, Model model, ServerWebExchange exchange) {
        if (exchange.getRequest().getQueryParams().containsKey("successMessage")) {
            model.addAttribute("successMessage", exchange.getRequest().getQueryParams().getFirst("successMessage"));
        }
        if (exchange.getRequest().getQueryParams().containsKey("errorMessage")) {
            model.addAttribute("errorMessage", exchange.getRequest().getQueryParams().getFirst("errorMessage"));
        }

        if (userDetails == null) {
            logger.warn("User not authenticated, redirecting to login.");
            return Mono.just("redirect:/login");
        }

        return Mono.fromCallable(() -> userService.findByUsername(userDetails.getUsername()))
                .flatMap(user -> {
                    if (user == null) {
                        logger.error("Authenticated user {} not found in database.", userDetails.getUsername());
                        model.addAttribute("errorMessage", "User not found. Please login again.");
                        model.addAttribute("isPortfolioEmpty", true);
                        model.addAttribute("portfolioAssets", new ArrayList<PortfolioEntryViewModel>());
                        model.addAttribute("totalPortfolioValue", BigDecimal.ZERO);
                        return Mono.just("redirect:/login?error=userNotFound");
                    }
                    try {
                        Portfolio portfolio = portfolioService.getPortfolioForUser(user);
                        model.addAttribute("portfolio", portfolio);

                        List<PortfolioEntryViewModel> portfolioAssets = new ArrayList<>();
                        BigDecimal totalCryptoValue = BigDecimal.ZERO;

                        if (portfolio != null && !portfolio.getEntries().isEmpty()) {
                            List<String> coinIds = portfolio.getEntries().stream()
                                    .map(PortfolioEntry::getCryptocurrencyId)
                                    .map(String::toLowerCase)
                                    .distinct()
                                    .collect(Collectors.toList());

                            return cryptoService.getCurrentPrices(coinIds, portfolio.getCurrency().toLowerCase())
                                .map(prices -> {
                                    BigDecimal currentTotalCryptoValue = BigDecimal.ZERO;
                                    for (PortfolioEntry entry : portfolio.getEntries()) {
                                        String normalizedEntryId = entry.getCryptocurrencyId().toLowerCase();
                                        Map<String, Double> priceData = prices.get(normalizedEntryId);
                                        BigDecimal currentPrice = BigDecimal.ZERO;
                                        if (priceData != null && priceData.containsKey(portfolio.getCurrency().toLowerCase())) {
                                            currentPrice = BigDecimal.valueOf(priceData.get(portfolio.getCurrency().toLowerCase()));
                                        }
                                        PortfolioEntryViewModel viewModel = new PortfolioEntryViewModel(entry, currentPrice);
                                        portfolioAssets.add(viewModel);
                                        currentTotalCryptoValue = currentTotalCryptoValue.add(viewModel.getCurrentMarketValue());
                                    }
                                    model.addAttribute("portfolioAssets", portfolioAssets);
                                    model.addAttribute("isPortfolioEmpty", portfolioAssets.isEmpty());
                                    BigDecimal totalPortfolioValue = portfolio.getBalance().add(currentTotalCryptoValue);
                                    model.addAttribute("totalPortfolioValue", totalPortfolioValue);
                                    model.addAttribute("userBalance", portfolio.getBalance());
                                    model.addAttribute("portfolioCurrency", portfolio.getCurrency());
                                    return "portfolio";
                                })
                                .defaultIfEmpty("portfolio")
                                .onErrorResume(priceError -> {
                                    logger.error("Error fetching prices for portfolio for user {}: {}", user.getUsername(), priceError.getMessage(), priceError);
                                    for (PortfolioEntry entry : portfolio.getEntries()) {
                                        portfolioAssets.add(new PortfolioEntryViewModel(entry, BigDecimal.ZERO)); 
                                    }
                                    model.addAttribute("portfolioAssets", portfolioAssets);
                                    model.addAttribute("isPortfolioEmpty", portfolioAssets.isEmpty());
                                    BigDecimal fallbackTotalValue = portfolio.getBalance();
                                    for(PortfolioEntryViewModel vm : portfolioAssets) {
                                        fallbackTotalValue = fallbackTotalValue.add(vm.getCurrentMarketValue());
                                    }
                                    model.addAttribute("totalPortfolioValue", fallbackTotalValue);
                                    model.addAttribute("userBalance", portfolio.getBalance());
                                    model.addAttribute("portfolioCurrency", portfolio.getCurrency());
                                    model.addAttribute("warningMessage", "Could not fetch current market prices. Displaying values based on purchase price or zero.");
                                    return Mono.just("portfolio");
                                });
                        } else {
                            model.addAttribute("portfolioAssets", portfolioAssets);
                            model.addAttribute("isPortfolioEmpty", true);
                            BigDecimal totalPortfolioValue = portfolio != null ? portfolio.getBalance() : BigDecimal.ZERO;
                            model.addAttribute("totalPortfolioValue", totalPortfolioValue);
                            if (portfolio != null) {
                                model.addAttribute("userBalance", portfolio.getBalance());
                                model.addAttribute("portfolioCurrency", portfolio.getCurrency());
                            } else {
                                model.addAttribute("userBalance", BigDecimal.ZERO);
                                model.addAttribute("portfolioCurrency", "N/A");
                            }
                            return Mono.just("portfolio");
                        }
                    } catch (Exception e) {
                        logger.error("Error fetching portfolio for user {}: {}", user.getUsername(), e.getMessage(), e);
                        model.addAttribute("errorMessage", "Could not load your portfolio. Please try again later.");
                        model.addAttribute("portfolio", null);
                        model.addAttribute("portfolioAssets", new ArrayList<PortfolioEntryViewModel>());
                        model.addAttribute("isPortfolioEmpty", true);
                        model.addAttribute("totalPortfolioValue", BigDecimal.ZERO);
                        return Mono.just("portfolio");
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Unexpected error in viewPortfolio for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
                    model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
                    model.addAttribute("portfolio", null);
                    model.addAttribute("portfolioAssets", new ArrayList<PortfolioEntryViewModel>());
                    model.addAttribute("isPortfolioEmpty", true);
                    model.addAttribute("totalPortfolioValue", BigDecimal.ZERO);
                    return Mono.just("portfolio");
                });
    }

    @PostMapping("/buy")
    public Mono<String> buyCrypto(@AuthenticationPrincipal UserDetails userDetails,
                                  ServerWebExchange exchange) {

        if (userDetails == null) {
            logger.warn("User not authenticated, redirecting to login.");
            return Mono.just("redirect:/login");
        }

        return exchange.getFormData().flatMap(formData -> {
            String cryptocurrencyId = formData.getFirst("cryptocurrencyId");
            String cryptocurrencyName = formData.getFirst("cryptocurrencyName");
            String cryptocurrencySymbol = formData.getFirst("cryptocurrencySymbol");
            String currentPriceStr = formData.getFirst("currentPrice");
            String amountStr = formData.getFirst("amount");

            if (cryptocurrencyId == null || cryptocurrencyId.isEmpty() ||
                cryptocurrencyName == null || cryptocurrencyName.isEmpty() ||
                cryptocurrencySymbol == null || cryptocurrencySymbol.isEmpty() ||
                currentPriceStr == null || currentPriceStr.isEmpty() ||
                amountStr == null || amountStr.isEmpty()) {
                logger.warn("Missing one or more required parameters for buying crypto by user {}", userDetails.getUsername());
                String errorMessage = "Missing required information for purchase.";
                try {
                    return Mono.just("redirect:/portfolio?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException ex) {
                    return Mono.just("redirect:/portfolio?errorMessage=Missing+required+information.");
                }
            }

            double currentPrice;
            java.math.BigDecimal amount;

            try {
                currentPrice = Double.parseDouble(currentPriceStr);
            } catch (NumberFormatException e) {
                logger.warn("Invalid currentPrice format '{}' for user {}", currentPriceStr, userDetails.getUsername());
                String errorMessage = "Invalid price format: " + currentPriceStr;
                try {
                    return Mono.just("redirect:/portfolio?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException ex) {
                    return Mono.just("redirect:/portfolio?errorMessage=Invalid+price+format.");
                }
            }

            try {
                amount = new java.math.BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                logger.warn("Invalid amount format '{}' for user {}", amountStr, userDetails.getUsername());
                String errorMessage = "Invalid amount format: " + amountStr;
                try {
                    return Mono.just("redirect:/portfolio?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException ex) {
                    return Mono.just("redirect:/portfolio?errorMessage=Invalid+amount+format.");
                }
            }

            if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                logger.warn("Attempt to buy non-positive amount {} of {} by user {}", amount, cryptocurrencyId, userDetails.getUsername());
                String errorMessage = "Amount must be positive.";
                try {
                    return Mono.just("redirect:/portfolio?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException ex) {
                    return Mono.just("redirect:/portfolio?errorMessage=Amount+must+be+positive.");
                }
            }
            
            String username = userDetails.getUsername();
            return Mono.fromCallable(() -> userService.findByUsername(username))
                .flatMap(user -> {
                    if (user == null) {
                        logger.error("Authenticated user {} not found in database.", username);
                        return Mono.just("redirect:/login?error=userNotFound");
                    }
                    return portfolioService.buyCryptocurrency(user, cryptocurrencyId, cryptocurrencyName, cryptocurrencySymbol, currentPrice, amount)
                        .then(Mono.fromCallable(() -> {
                            logger.info("User {} successfully bought {} of {}", username, amount, cryptocurrencyId);
                            String successMessage = "Successfully bought " + amount + " " + cryptocurrencySymbol.toUpperCase();
                            try {
                                return "redirect:/portfolio?successMessage=" + URLEncoder.encode(successMessage, StandardCharsets.UTF_8.toString());
                            } catch (UnsupportedEncodingException e) {
                                return "redirect:/portfolio?successMessage=Purchase+successful.";
                            }
                        }))
                        .onErrorResume(PortfolioService.InsufficientFundsException.class, e -> {
                            logger.warn("User {} has insufficient funds to buy {} of {}: {}", username, amount, cryptocurrencyId, e.getMessage());
                            try {
                                return Mono.just("redirect:/portfolio?errorMessage=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
                            } catch (UnsupportedEncodingException ex) {
                                return Mono.just("redirect:/portfolio?errorMessage=Insufficient+funds.");
                            }
                        })
                        .onErrorResume(RuntimeException.class, e -> {
                            logger.error("Error buying crypto for user {}: {}", username, e.getMessage(), e);
                            String errorMessage = "Could not buy cryptocurrency. " + e.getMessage();
                            try {
                                return Mono.just("redirect:/portfolio?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString()));
                            } catch (UnsupportedEncodingException ex) {
                                return Mono.just("redirect:/portfolio?errorMessage=Error+during+purchase.");
                            }
                        });
                });
        }).onErrorResume(e -> {
            logger.error("Unexpected error processing buyCrypto for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            String errorMessage = "An unexpected error occurred during purchase processing.";
            try {
                return Mono.just("redirect:/portfolio?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException ex) {
                return Mono.just("redirect:/portfolio?errorMessage=Unexpected+error.");
            }
        });
    }
} 