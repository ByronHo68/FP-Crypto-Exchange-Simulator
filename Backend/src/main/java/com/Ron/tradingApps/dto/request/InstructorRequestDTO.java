package com.Ron.tradingApps.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InstructorRequestDTO {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userId;
    private String instructorNumber;
}
