package com.Ron.tradingApps.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletResponseDTO {
    private Integer USDTWalletId;
    private Integer currencyWalletId;
    private Integer traderId;
    private String currency;
    private BigDecimal USDTAmount;
    private BigDecimal currencyAmount;
    private LocalDateTime updatedAt;
}