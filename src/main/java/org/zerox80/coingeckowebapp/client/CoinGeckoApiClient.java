package org.zerox80.coingeckowebapp.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.zerox80.coingeckowebapp.model.CryptoCurrency;
import reactor.util.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import reactor.core.publisher.Mono;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;

@Component
public class CoinGeckoApiClient {

    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;
    private static final Logger logger = LoggerFactory.getLogger(CoinGeckoApiClient.class);

    @Value("${api.coingecko.base-url}")
    private String apiBaseUrl;

    @Value("${api.coingecko.retry-delay-ms:1000}")
    private long retryDelayMs;

    @Value("${api.coingecko.max-retries:3}")
    private int maxRetries;

    public CoinGeckoApiClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder.baseUrl(apiBaseUrl).build();
    }

    public Mono<List<CryptoCurrency>> getCoins(String vsCurrency, int count) {
        String path = "/coins/markets?vs_currency={vsCurrency}&order=market_cap_desc&per_page={count}&page=1&sparkline=false&price_change_percentage=24h";
        String fullUrl = apiBaseUrl + path.replace("{vsCurrency}", vsCurrency).replace("{count}", String.valueOf(count));
        logger.info("Attempting to fetch data from URL: {}", fullUrl);

        return webClient.get()
                .uri(path, vsCurrency, count)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .doOnNext(errorBody -> logger.error("API Error Response ({}): {}", clientResponse.statusCode(), errorBody))
                            .flatMap(errorBody -> Mono.error(new RuntimeException("API call failed with status " + clientResponse.statusCode() + ": " + errorBody)));
                })
                .bodyToFlux(CryptoCurrency.class)
                .collectList()
                .retryWhen(Retry.fixedDelay(maxRetries, Duration.ofMillis(retryDelayMs))
                        .doBeforeRetry(retrySignal -> logger.warn("Retrying API call, attempt #{}. Cause: {}", 
                                retrySignal.totalRetries() + 1, 
                                retrySignal.failure().getMessage()))
                )
                .doOnError(error -> logger.error("Failed to fetch data after retries from URL: {}. Error: {}", fullUrl, error.getMessage()));
    }

    public Mono<Map<String, Map<String, Double>>> getPricesForIds(List<String> coinIds, String vsCurrency) {
        String ids = String.join(",", coinIds);
        String path = "/simple/price?ids={ids}&vs_currencies={vsCurrency}";
        String fullUrl = apiBaseUrl + path.replace("{ids}", ids).replace("{vsCurrency}", vsCurrency);
        logger.info("Attempting to fetch prices from URL: {}", fullUrl);

        return webClient.get()
                .uri(path, ids, vsCurrency)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                            .doOnNext(errorBody -> logger.error("API Error Response ({}): {} for URL {}", clientResponse.statusCode(), errorBody, fullUrl))
                            .flatMap(errorBody -> Mono.error(new RuntimeException("API call failed with status " + clientResponse.statusCode() + ": " + errorBody + " for URL " + fullUrl)))
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, Map<String, Double>>>() {})
                .retryWhen(Retry.fixedDelay(maxRetries, Duration.ofMillis(retryDelayMs))
                        .doBeforeRetry(retrySignal -> logger.warn("Retrying API call for prices, attempt #{}. Cause: {} for URL: {}",
                                retrySignal.totalRetries() + 1,
                                retrySignal.failure().getMessage(), fullUrl))
                )
                .doOnError(error -> logger.error("Failed to fetch prices after retries from URL: {}. Error: {}", fullUrl, error.getMessage()));
    }
}