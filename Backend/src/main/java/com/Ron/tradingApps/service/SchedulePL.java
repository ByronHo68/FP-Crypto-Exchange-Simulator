package com.Ron.tradingApps.service;

import com.Ron.tradingApps.dto.TraderDTO;
import com.Ron.tradingApps.model.Order;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.model.Wallet;
import com.Ron.tradingApps.model.historicalData.Candle;
import com.Ron.tradingApps.repository.CandleRepository;
import com.Ron.tradingApps.repository.OrderRepository;
import com.Ron.tradingApps.repository.TraderRepository;
import com.Ron.tradingApps.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@Slf4j
@EnableScheduling
public class SchedulePL {

    @Autowired
    private TraderRepository traderRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CandleRepository candleRepository;
    private static final BigDecimal MAX_ALLOWED_TOTAL = new BigDecimal("9999999999999999.99");
    private static final String BUY_TYPE = "buy";
    private static final String SELL_TYPE = "sell";

    @Scheduled(cron = "0 2 0 * * *", zone = "Asia/Hong_Kong")
    public void run() throws Exception {
        log.info("Scheduled task started at 00:02 HKT.");
        checkPL();
    }

    public void checkPL() {
        log.info("Checking profit and loss for all traders.");

        List<Trader> allTraders = traderRepository.findAll();
        log.info("Total traders found: {}", allTraders.size());

        for (Trader trader : allTraders) {
            int traderId = trader.getId();
            log.info("Processing trader ID: {}", traderId);
            List<Wallet> allWallets = walletRepository.findByTraderId(traderId);
            List<Order> pendingOrders = orderRepository.findPendingOrdersByTraderId(traderId);
            log.info("Total wallets and pending orders found for trader ID {}: {} and {}", traderId, allWallets.size(), pendingOrders.size());

            List<BigDecimal> total = new ArrayList<>();
            LocalDateTime midNight = LocalDateTime.now().minusDays(1).withHour(0).withMinute(1).withSecond(0).withNano(0);

            for (Wallet wallet : allWallets) {
                if ("USDT".equals(wallet.getCurrency())) {
                    total.add(wallet.getAmount());
                    log.debug("Added USDT amount: {}", wallet.getAmount());
                } else {
                    BigDecimal walletAmount = wallet.getAmount();
                    String walletCurrency = wallet.getCurrency();
                    log.info("Querying close price for currency {} at time {}", walletCurrency, midNight);

                    Optional<BigDecimal> priceOpt = candleRepository.findClosePriceBySymbolAndOpenTime(walletCurrency, midNight);
                    if (priceOpt.isPresent()) {
                        BigDecimal price = priceOpt.get();
                        total.add(price.multiply(walletAmount));
                        log.debug("Added amount for currency {}: {} * {} = {}", walletCurrency, price, walletAmount, price.multiply(walletAmount));
                    } else {
                        log.warn("No close price found for currency: {} at time: {}", walletCurrency, midNight);
                    }
                }
            }


            for (Order order : pendingOrders) {
                Optional<BigDecimal> priceOpt = candleRepository.findClosePriceBySymbolAndOpenTime(order.getCurrency(), midNight);
                BigDecimal price = priceOpt.orElse(BigDecimal.ZERO);
                if (order.getBuyAndSellType().equalsIgnoreCase(SELL_TYPE)) {
                    total.add(price.multiply(order.getAmount()));
                } else if (order.getBuyAndSellType().equalsIgnoreCase(BUY_TYPE)) {
                    total.add(order.getPrice().multiply(order.getAmount()));
                }
            }

            BigDecimal sum = total.stream().reduce(BigDecimal.ZERO, BigDecimal::add);


            if (sum.compareTo(MAX_ALLOWED_TOTAL) > 0) {
                log.error("Calculated total exceeds maximum allowed value for trader ID {}: {}", traderId, sum);
                continue;
            }

            trader.setYesterdayPrice(sum);

            try {
                traderRepository.save(trader);
                log.info("Trader ID {}: Total yesterday price calculated: {}", traderId, sum);
            } catch (Exception e) {
                log.error("Failed to save trader ID {} with yesterday price {}: {}", traderId, sum, e.getMessage());
            }
        }
    }
}