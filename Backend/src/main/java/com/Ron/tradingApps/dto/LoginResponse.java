package com.Ron.tradingApps.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String userId;

    public LoginResponse(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }
}
