package org.zerox80.coingeckowebapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;
import org.zerox80.coingeckowebapp.service.CryptoService;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
public class CryptoController {

    private static final Logger logger = LoggerFactory.getLogger(CryptoController.class);
    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/")
    public Mono<String> index(@RequestParam(value = "currency", defaultValue = "eur") String currency,
                              @RequestParam(value = "count", defaultValue = "10") int count,
                              Model model) {
        logger.info("Request received for currency: {} and count: {}", currency, count);

        int effectiveCount = Math.max(1, Math.min(count, 250));

        if (count > 250) {
            logger.warn("Requested count {} exceeds maximum allowed (250). Capping to 250.", count);
            model.addAttribute("warningMessage", "Requested count was capped at 250.");
        } else if (count < 1) {
             logger.warn("Requested count {} is less than minimum allowed (1). Setting to 1.", count);
             model.addAttribute("warningMessage", "Requested count was set to 1.");
        }

        model.addAttribute("currency", currency);
        model.addAttribute("count", effectiveCount);

        return cryptoService.getTopCryptocurrencies(currency, effectiveCount)
                .doOnNext(cryptocurrencies -> {
                    model.addAttribute("cryptocurrencies", cryptocurrencies);
                })
                .thenReturn("index")
                .onErrorResume(e -> {
                    logger.error("Error fetching cryptocurrencies: {}", e.getMessage(), e);
                    model.addAttribute("errorMessage", "Failed to fetch cryptocurrency data. Please try again later.");
                    model.addAttribute("cryptocurrencies", List.of());
                    return Mono.just("index");
                });
    }
}