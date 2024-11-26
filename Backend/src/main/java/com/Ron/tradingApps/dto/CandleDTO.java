package com.Ron.tradingApps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandleDTO {
    private String symbol;
    private long openTime;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private double volume;
    private String formattedTime;
    private LocalDate date;
    private String formattedOpenTime;
}