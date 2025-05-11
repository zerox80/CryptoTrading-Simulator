package org.zerox80.coingeckowebapp.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PortfolioEntryViewModel {
    private PortfolioEntry entry;
    private BigDecimal currentPrice;
    private BigDecimal currentMarketValue;
    private BigDecimal profitOrLoss;
    private BigDecimal profitOrLossPercentage;

    public PortfolioEntryViewModel(PortfolioEntry entry, BigDecimal currentPrice) {
        this.entry = entry;
        this.currentPrice = currentPrice != null ? currentPrice : BigDecimal.ZERO;

        if (entry != null && entry.getAmount() != null) {
            this.currentMarketValue = entry.getAmount().multiply(this.currentPrice);
            if (entry.getAveragePurchasePrice() != null) {
                BigDecimal totalPurchaseCost = entry.getAmount().multiply(entry.getAveragePurchasePrice());
                this.profitOrLoss = this.currentMarketValue.subtract(totalPurchaseCost);
                if (totalPurchaseCost.compareTo(BigDecimal.ZERO) != 0) {
                    this.profitOrLossPercentage = this.profitOrLoss
                            .divide(totalPurchaseCost, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(new BigDecimal("100"));
                } else {
                    if (this.currentMarketValue.compareTo(BigDecimal.ZERO) > 0) {
                        this.profitOrLossPercentage = new BigDecimal("100");
                    } else if (this.currentMarketValue.compareTo(BigDecimal.ZERO) < 0) {
                        this.profitOrLossPercentage = new BigDecimal("-100");
                    } else {
                        this.profitOrLossPercentage = BigDecimal.ZERO;
                    }
                }
            } else {
                this.profitOrLoss = this.currentMarketValue;
                this.profitOrLossPercentage = this.currentMarketValue.compareTo(BigDecimal.ZERO) != 0 ? new BigDecimal("100") : BigDecimal.ZERO;
            }
        } else {
            this.currentMarketValue = BigDecimal.ZERO;
            this.profitOrLoss = BigDecimal.ZERO;
            this.profitOrLossPercentage = BigDecimal.ZERO;
        }
    }

    public String getCryptocurrencyName() { return entry.getCryptocurrencyName(); }
    public String getCryptocurrencySymbol() { return entry.getCryptocurrencySymbol(); }
    public BigDecimal getAmount() { return entry.getAmount(); }
    public BigDecimal getAveragePurchasePrice() { return entry.getAveragePurchasePrice(); }
    public Long getId() { return entry.getId(); }
} 