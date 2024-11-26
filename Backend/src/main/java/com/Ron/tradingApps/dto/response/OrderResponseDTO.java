package com.Ron.tradingApps.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class OrderResponseDTO {
    private Integer id;
    private Integer traderId;
    private String marketOrLimitOrderTypes;
    private String buyAndSellType;
    private String currency;
    private BigDecimal price;
    private BigDecimal amount;
    private String orderStatus;
    private LocalDateTime updateTime;

    /*public OrderResponseDTO(Integer id, Integer traderId, String marketOrLimitOrderTypes,
                            String buyAndSellType, String currency, BigDecimal price,
                            BigDecimal amount, String orderStatus, LocalDateTime updateTime) {
        this.id = id;
        this.traderId = traderId;
        this.marketOrLimitOrderTypes = marketOrLimitOrderTypes;
        this.buyAndSellType = buyAndSellType;
        this.currency = currency;
        this.price = price;
        this.amount = amount;
        this.orderStatus = orderStatus;
        this.updateTime = updateTime;
    }*/


}