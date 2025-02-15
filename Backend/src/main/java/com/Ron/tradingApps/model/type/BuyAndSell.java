package com.Ron.tradingApps.model.type;

import lombok.Data;

@Data
public class BuyAndSell {
    public enum Type{
        Buy,
        Sell;
        private String type;
        public String getType(){return type;}
    }
}
