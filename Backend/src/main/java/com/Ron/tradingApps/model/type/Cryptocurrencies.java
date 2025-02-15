package com.Ron.tradingApps.model.type;

import lombok.Data;

@Data
public class Cryptocurrencies {
    private Type type;
    public enum Type{
        BTCUSDT,
        ETHUSDT;
        /*SOLUSDT;*/

        private String type;
        public String getType(){return type;}
    }
}
