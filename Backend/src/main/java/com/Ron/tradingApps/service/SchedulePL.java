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
import java.util.*;

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
    private static final String BTCUSDT = "BTCUSDT";
    private static final String ETHUSDT = "ETHUSDT";

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Hong_Kong")
    public void run() throws Exception {
        log.info("Scheduled task started at 00:05 HKT.");
        checkPL();
    }

    public void checkPL() {
        log.info("Checking profit and loss for all traders.");
        Map<String, BigDecimal> priceCache = new HashMap<>();
        LocalDateTime midNight = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        priceCache.put("USDT", BigDecimal.ONE);
        Optional<BigDecimal> btcPriceOpt = candleRepository.findClosePriceBySymbolAndOpenTime("BTCUSDT", midNight);
        btcPriceOpt.ifPresent(price -> priceCache.put("BTCUSDT", price));
        Optional<BigDecimal> ethPriceOpt = candleRepository.findClosePriceBySymbolAndOpenTime("ETHUSDT", midNight);
        ethPriceOpt.ifPresent(price -> priceCache.put("ETHUSDT", price));


        List<Trader> allTraders = traderRepository.findAll();
        log.info("Total traders found: {}", allTraders.size());


        for (Trader trader : allTraders) {
            int traderId = trader.getId();
            log.info("Processing trader ID: {}", traderId);
            List<Wallet> allWallets = walletRepository.findByTraderId(traderId);
            List<Order> pendingOrders = orderRepository.findPendingOrdersByTraderId(traderId);
            log.info("Total wallets and pending orders found for trader ID {}: {} and {}", traderId, allWallets.size(), pendingOrders.size());

            List<BigDecimal> total = new ArrayList<>();

            for (Wallet wallet : allWallets) {
                String walletCurrency = wallet.getCurrency();
                BigDecimal walletAmount = wallet.getAmount();

                if ("USDT".equals(walletCurrency)) {
                    total.add(walletAmount);
                } else {
                    BigDecimal price = priceCache.getOrDefault(walletCurrency, BigDecimal.ZERO);
                    total.add(price.multiply(walletAmount));
                }
            }

            for (Order order : pendingOrders) {
                BigDecimal price = priceCache.getOrDefault(order.getCurrency(), BigDecimal.ZERO);
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