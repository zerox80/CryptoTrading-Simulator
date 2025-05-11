package org.zerox80.coingeckowebapp.service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.zerox80.coingeckowebapp.client.CoinGeckoApiClient;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class CryptoService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);
    private final CoinGeckoApiClient apiClient;

    public CryptoService(CoinGeckoApiClient apiClient) {
        this.apiClient = apiClient;
    }
    @Cacheable(value="cryptoData", key = "#currency + '-' + #count")
    public Mono<List<CryptoCurrency>> getTopCryptocurrencies(String currency, int count) {
        logger.info("Fetching from API for: {}, Count: {}", currency, count);
        return apiClient.getCoins(currency, count);
    }

    @Cacheable(value="currentPrices", key = "T(String).join(',', #coinIds) + '-' + #currency")
    public Mono<Map<String, Map<String, Double>>> getCurrentPrices(List<String> coinIds, String currency) {
        if (coinIds == null || coinIds.isEmpty()) {
            logger.warn("Attempted to fetch current prices with null or empty coinIds list.");
            return Mono.just(Map.of());
        }
        logger.info("Fetching current prices from API for coin IDs: {} in currency: {}", coinIds, currency);
        return apiClient.getPricesForIds(coinIds, currency);
    }
}