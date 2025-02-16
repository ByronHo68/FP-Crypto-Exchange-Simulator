package com.Ron.tradingApps.model.type;

import lombok.Data;
import lombok.Getter;

@Data
public class MarketAndLimit {
    private Type type;
    public enum Type {
        Market,
        limit;
        private String type;
    }

}
