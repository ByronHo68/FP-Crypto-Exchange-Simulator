package com.Ron.tradingApps.model.type;

import lombok.Data;

@Data
public class MarketAndLimit {
    private Type type;
    public enum Type {
        Market,
        limit;
        private String type;
        public String getType(){return type;}
    }

}
