package com.Ron.tradingApps.model.type;

import lombok.Getter;

@Getter
public enum MarketAndLimit {
    MARKET("market"),
    LIMIT("limit"),
    PENDING("Pending");
    private final String value;

    MarketAndLimit(String value) {
        this.value = value;
    }

    public static MarketAndLimit fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        for (MarketAndLimit type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown market type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
