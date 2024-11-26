package com.Ron.tradingApps.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TraderRequestDTO {
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
        private String PhoneNumber;
        private BigDecimal yesterdayPrice;
}
