package org.zerox80.coingeckowebapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableCaching
public class CoinGeckoWebApplication {
    private static final Logger logger = LoggerFactory.getLogger(CoinGeckoWebApplication.class);
    public static void main(String[] args) {
        logger.info("Starting CoinGeckoWebApplication");
        SpringApplication.run(CoinGeckoWebApplication.class, args);
        logger.info("CoinGeckoWebApplication started successfully");
    }
}
