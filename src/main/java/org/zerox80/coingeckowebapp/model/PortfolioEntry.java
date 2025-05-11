package org.zerox80.coingeckowebapp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_entries")
@Data
@NoArgsConstructor
public class PortfolioEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false)
    private String cryptocurrencyId;

    @Column(nullable = false)
    private String cryptocurrencySymbol;

    @Column(nullable = false)
    private String cryptocurrencyName;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal averagePurchasePrice;

    public PortfolioEntry(Portfolio portfolio, String cryptocurrencyId, String cryptocurrencySymbol, String cryptocurrencyName, BigDecimal amount, BigDecimal averagePurchasePrice) {
        this.portfolio = portfolio;
        this.cryptocurrencyId = cryptocurrencyId;
        this.cryptocurrencySymbol = cryptocurrencySymbol;
        this.cryptocurrencyName = cryptocurrencyName;
        this.amount = amount;
        this.averagePurchasePrice = averagePurchasePrice;
    }
} 