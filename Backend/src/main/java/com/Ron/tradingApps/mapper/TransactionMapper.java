package com.Ron.tradingApps.mapper;

import com.Ron.tradingApps.dto.TransactionDTO;
import com.Ron.tradingApps.dto.response.TransactionResponseDTO;
import com.Ron.tradingApps.model.Transaction;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.model.Wallet;
import com.Ron.tradingApps.model.Order;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public static Transaction toEntity(TransactionDTO dto, Trader trader, Wallet wallet, Order order) {
        Transaction transaction = new Transaction();
        transaction.setTrader(trader);
        transaction.setWallet(wallet);
        transaction.setOrder(order);
        transaction.setBuyOrSellType(dto.getBuyOrSellType());
        transaction.setPrice(dto.getPrice());
        transaction.setCurrency(dto.getCurrency());
        transaction.setAmount(dto.getAmount());
        return transaction;
    }

    public TransactionResponseDTO toDTO(Transaction transaction){
        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .traderId(transaction.getTrader().getId())
                .walletId(transaction.getWallet().getId())
                .orderId(transaction.getOrder().getId())
                .buyOrSellType(transaction.getBuyOrSellType())
                .price(transaction.getPrice())
                .currency(transaction.getCurrency())
                .amount(transaction.getAmount())
                .build();
    }
}