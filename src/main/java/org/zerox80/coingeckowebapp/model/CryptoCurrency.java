package org.zerox80.coingeckowebapp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;

@Getter
@ToString
@EqualsAndHashCode
public class CryptoCurrency {

    private final String id;
    private final String symbol;
    private final String name;
    private final String image;
    private final double currentPrice;
    private final double marketCap;
    private final double priceChangePercentage24h;

    @JsonCreator
    public CryptoCurrency(
            @JsonProperty("id") String id,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("name") String name,
            @JsonProperty("image") String image,
            @JsonProperty("current_price") double currentPrice,
            @JsonProperty("market_cap") double marketCap,
            @JsonProperty("price_change_percentage_24h") double priceChangePercentage24h) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (image == null || image.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        try {
            new URI(image);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid image URL format: " + image, e);
        }

        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.marketCap = marketCap;
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public String getMarketCapFormatted() {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        if (marketCap >= 1_000_000_000) {
            return formatter.format(marketCap / 1_000_000_000) + " B";
        } else if (marketCap >= 1_000_000) {
            return formatter.format(marketCap / 1_000_000) + " M";
        } else {
            return formatter.format(marketCap);
        }
    }
}