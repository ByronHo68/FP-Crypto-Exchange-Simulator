package com.Ron.tradingApps.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id", nullable = false)
    private Trader trader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "buy_or_sell_type", nullable = false)
    private String buyOrSellType;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Builder
    public Transaction(Trader trader, Wallet wallet, Order order, String buyOrSellType, BigDecimal price, String currency, BigDecimal amount, LocalDateTime timestamp) {
        this.trader = trader;
        this.wallet = wallet;
        this.order = order;
        this.buyOrSellType = buyOrSellType;
        this.price = price;
        this.currency = currency;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}