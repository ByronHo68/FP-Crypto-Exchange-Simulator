package com.Ron.tradingApps.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDTO {
    private Integer traderId;
    private BigDecimal amount;
}