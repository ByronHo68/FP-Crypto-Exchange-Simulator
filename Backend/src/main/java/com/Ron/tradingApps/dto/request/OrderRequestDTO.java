package com.Ron.tradingApps.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequestDTO {
    private Integer traderId;
    private String marketOrLimitOrderTypes;
    private String buyAndSellType;
    private String currency;
    private BigDecimal price;
    private BigDecimal amount;
}