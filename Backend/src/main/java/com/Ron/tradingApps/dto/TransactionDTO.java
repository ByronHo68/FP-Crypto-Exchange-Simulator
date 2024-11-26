package com.Ron.tradingApps.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class TransactionDTO {
    private Integer traderId;
    private Integer walletId;
    private Integer orderId;
    private String buyOrSellType;
    private String currency;
    private BigDecimal price;
    private BigDecimal amount;
    private LocalDateTime timestamp;
}