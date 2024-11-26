package com.Ron.tradingApps.model;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.checker.units.qual.A;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchConnectionDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trader")
public class Trader extends AppsUser {


    @Column(name = "usdt_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal usdtBalance = BigDecimal.ZERO;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "yesterday_price", precision = 20, scale = 2)
    private BigDecimal yesterdayPrice = BigDecimal.ZERO;

    @Builder
    public Trader(String username, String email,
                  String password, String firstName,
                  String lastName, LocalDateTime createdAt,
                  LocalDateTime updatedAt, String userId, BigDecimal usdtBalance,
                  String idNumber, String phoneNumber,
                  BigDecimal yesterdayPrice) {
        super(username, email, password, firstName, lastName, createdAt, updatedAt, userId);
        this.usdtBalance = usdtBalance;
        this.idNumber = idNumber;
        this.phoneNumber = phoneNumber;
        this.yesterdayPrice = yesterdayPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trader trader)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getUsdtBalance(), trader.getUsdtBalance()) && Objects.equals(getIdNumber(), trader.getIdNumber()) && Objects.equals(getPhoneNumber(), trader.getPhoneNumber()) && Objects.equals(getYesterdayPrice(), trader.getYesterdayPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getUsdtBalance(), getIdNumber(), getPhoneNumber(), getYesterdayPrice());
    }
}
