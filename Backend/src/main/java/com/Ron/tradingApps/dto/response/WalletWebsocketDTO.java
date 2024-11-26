package com.Ron.tradingApps.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletWebsocketDTO {
    private int id;
    private int traderId;
    private String currency;
    private BigDecimal amount;
}
