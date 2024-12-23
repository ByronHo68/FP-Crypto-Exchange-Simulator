package com.Ron.tradingApps.service;

import com.Ron.tradingApps.dto.response.OrderResponseDTO;
import com.Ron.tradingApps.dto.response.WalletDResponseDTO;
import com.Ron.tradingApps.mapper.WalletMapper;
import com.Ron.tradingApps.model.Order;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.dto.WalletDTO;
import com.Ron.tradingApps.model.Wallet;
import com.Ron.tradingApps.repository.TraderRepository;
import com.Ron.tradingApps.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TraderRepository traderRepository;

    @Autowired
    private TraderService traderService;
    @Autowired
    private WalletMapper walletMapper;

    private static final String CURRENCY_USDT = "USDT";

    public Wallet createWallet(Trader trader, String currency) {
        Wallet wallet = Wallet.builder()
                .trader(trader)
                .currency(currency)
                .amount(BigDecimal.ZERO)
                .build();
        return walletRepository.save(wallet);
    }

    public Wallet findOrCreateWallet(Trader trader, String currency) {
        return walletRepository.findByTraderIdAndCurrency(trader.getId(), currency)
                .orElseGet(() -> createWallet(trader, currency));
    }

    public void transferUsdtToWallet(Trader trader, BigDecimal amount) {
        Wallet usdtWallet = findOrCreateWallet(trader, CURRENCY_USDT);

        if (trader.getUsdtBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient USDT balance in trader's account");
        }

        trader.setUsdtBalance(trader.getUsdtBalance().subtract(amount));

        usdtWallet.setAmount(usdtWallet.getAmount().add(amount));

        walletRepository.save(usdtWallet);
        traderRepository.save(trader);
    }

    public Wallet findByIdAndCurrencyOrCreate(Integer traderId, String currency) {
        return walletRepository.findByTraderIdAndCurrency(traderId, currency)
                .orElseGet(() -> createWallet(traderService.findById(traderId), currency));
    }

    public List<WalletDResponseDTO> getWalletsForTrader(String userId) {
        try {
            Optional<Trader> trader = traderRepository.findByUserId(userId);
            if (!trader.isPresent()) {
                throw new IllegalArgumentException("Trader not found for user ID: " + userId);
            }

            int traderId = trader.get().getId();

            List<Wallet> wallets = walletRepository.findByTraderId(traderId);
            System.out.println("Retrieved " + wallets.size() + " wallets for trader ID: " + traderId);

            return wallets.stream()
                    .map(wallet -> WalletDResponseDTO.builder()
                            .currencyWalletId(wallet.getId())
                            .traderId(wallet.getTrader().getId())
                            .currency(wallet.getCurrency())
                            .USDTAmount(wallet.getAmount())
                            .currencyAmount(wallet.getAmount())
                            .updatedAt(wallet.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error retrieving wallets for user ID: " + userId);
            e.printStackTrace();
            throw e;
        }
    }
    public List<WalletDTO> getAllWallets() {
        List<Wallet> wallets = walletRepository.findAll();
        return wallets.stream()
                .map(walletMapper::toDTO)
                .collect(Collectors.toList());
    }
}