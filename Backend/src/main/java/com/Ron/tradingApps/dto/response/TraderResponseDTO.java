package com.Ron.tradingApps.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class TraderResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;

    private BigDecimal usdtBalance;
    private String idNumber;
    private String phoneNumber;
    private BigDecimal yesterdayPrice;
}
