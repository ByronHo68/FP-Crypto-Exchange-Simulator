package com.Ron.tradingApps.mapper;

import com.Ron.tradingApps.dto.WalletDTO;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.model.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public static Wallet toEntity(WalletDTO dto, Trader trader) {
        Wallet wallet = new Wallet();
        wallet.setTrader(trader);
        wallet.setCurrency(dto.getCurrency());
        wallet.setAmount(dto.getAmount());
        return wallet;
    }

    public  WalletDTO toDTO(Wallet wallet) {
        return WalletDTO.builder()
                .id(wallet.getId())
                .traderId(wallet.getTrader().getId())
                .currency(wallet.getCurrency())
                .amount(wallet.getAmount())
                .build();
    }
}
