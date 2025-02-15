package com.Ron.tradingApps.service;

import com.Ron.tradingApps.dto.request.OrderRequestDTO;
import com.Ron.tradingApps.model.type.BuyAndSell;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.model.Wallet;
import com.Ron.tradingApps.repository.TraderRepository;
import com.Ron.tradingApps.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class BalanceCheckService {

    @Autowired
    private TraderRepository traderRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletService walletService;

@Transactional
    public void balanceCheck(OrderRequestDTO requestDTO) {

        Trader trader = traderRepository.findById(requestDTO.getTraderId())
                .orElseThrow(() -> new IllegalArgumentException("Trader not found"));

        Wallet usdtWallet = walletService.findOrCreateWallet(trader, "USDT");
        Wallet wallet = walletService.findOrCreateWallet(trader, requestDTO.getCurrency());


        BigDecimal totalCost = requestDTO.getPrice().multiply(requestDTO.getAmount());
        if (String.valueOf(BuyAndSell.Type.Buy).equalsIgnoreCase(requestDTO.getBuyAndSellType())) {
            if (usdtWallet.getAmount().compareTo(totalCost) > 0) {
                return;
                /*// Deduct USDT from trader's balance
                usdtWallet.setAmount(usdtWallet.getAmount().subtract(totalCost));
                // Increase amount in wallet
                wallet.setAmount(wallet.getAmount().add(requestDTO.getAmount()));

                *//*createTransaction(trader, wallet, order, requestDTO);*/
            } else {
                throw new IllegalArgumentException("Insufficient USDT balance");
            }
        }

        // Check sell order
        if (String.valueOf(BuyAndSell.Type.Sell).equalsIgnoreCase(requestDTO.getBuyAndSellType())) {
            if (requestDTO.getAmount().compareTo(wallet.getAmount()) < 0) {
                return;
                /*// Deduct amount from wallet
                wallet.setAmount(wallet.getAmount().subtract(requestDTO.getAmount()));
                // Add to trader's USDT balance based on market price
                usdtWallet.setAmount(usdtWallet.getAmount().add(totalCost));

                *//*createTransaction(trader, wallet, order, requestDTO);*/
            } else {
                throw new IllegalArgumentException("Insufficient currency balance");
            }
        }
    }
}
