package com.Ron.tradingApps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDTO {
    private Integer id;
    private Integer traderId;
    private String currency;
    private BigDecimal amount;

}