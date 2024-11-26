package com.Ron.tradingApps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {
    private Integer id;
    private Integer traderId;
    private BigDecimal price;
    private BigDecimal amount;
    private String buyAndSellType;
    private String currency;
    private String marketOrLimitOrderTypes;
    private String orderStatus;
}
