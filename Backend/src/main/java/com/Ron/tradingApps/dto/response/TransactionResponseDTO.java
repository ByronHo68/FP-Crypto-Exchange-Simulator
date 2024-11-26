package com.Ron.tradingApps.dto.response;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponseDTO {
    private Integer id;
    private Integer traderId;
    private Integer walletId;
    private Integer orderId;
    private String buyOrSellType;
    private BigDecimal price;
    private String currency;
    private BigDecimal amount;
    private String orderStatus;
    private LocalDateTime timestamp;
}