package com.Ron.tradingApps.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraderDashboardResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String userId;
    private String phoneNumber;
    private BigDecimal usdtBalance;
    private BigDecimal yesterdayPrice;
}
