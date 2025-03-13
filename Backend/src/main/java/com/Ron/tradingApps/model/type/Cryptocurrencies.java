package com.Ron.tradingApps.model.type;

import lombok.Data;
import lombok.Getter;

@Data
public class Cryptocurrencies {
    private Type type;
    @Getter
    public enum Type{
        BTCUSDT,
        ETHUSDT;
        /*SOLUSDT;*/

        private String type;
    }
}
