package com.Ron.tradingApps.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id", nullable = false)
    private Trader trader;

    @Column(name = "buy_and_sell_type", nullable = false)
    private String buyAndSellType;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(name = "market_or_limit_order_types", nullable = false)
    private String marketOrLimitOrderTypes;

    @Column(name = "order_status", nullable = false, columnDefinition = "VARCHAR(20)")
    private String orderStatus = "Pending";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Order(Trader trader, String buyAndSellType, String currency, BigDecimal amount, BigDecimal price, String marketOrLimitOrderTypes, String orderStatus) {
        this.trader = trader;
        this.buyAndSellType = buyAndSellType;
        this.currency = currency;
        this.amount = amount;
        this.price = price;
        this.marketOrLimitOrderTypes = marketOrLimitOrderTypes;
        this.orderStatus = orderStatus;
    }
    @Getter
    public enum BuyAndSellType{
        Buy,
        Sell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return getId() == order.getId() && Objects.equals(getTrader(), order.getTrader()) && Objects.equals(getBuyAndSellType(), order.getBuyAndSellType()) && Objects.equals(getCurrency(), order.getCurrency()) && Objects.equals(getAmount(), order.getAmount()) && Objects.equals(getPrice(), order.getPrice()) && Objects.equals(getMarketOrLimitOrderTypes(), order.getMarketOrLimitOrderTypes()) && Objects.equals(getOrderStatus(), order.getOrderStatus()) && Objects.equals(getCreatedAt(), order.getCreatedAt()) && Objects.equals(getUpdatedAt(), order.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTrader(), getBuyAndSellType(), getCurrency(), getAmount(), getPrice(), getMarketOrLimitOrderTypes(), getOrderStatus(), getCreatedAt(), getUpdatedAt());
    }
}
