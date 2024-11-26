package com.Ron.tradingApps.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignUpRequestDTO {
    @NotBlank(message = "username must not be blank")
    private String displayName;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password must not be blank")
    private String password;

    public String secretCode;
}
