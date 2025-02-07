package com.Ron.tradingApps.service.data;

import com.Ron.tradingApps.model.Cryptocurrencies;
import com.Ron.tradingApps.model.historicalData.Candle;
import com.Ron.tradingApps.repository.CandleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PriceService {

    @Autowired
    private CandleRepository candleRepository;
    private final Map<String,Double> priceMap = new HashMap<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Random random = new Random();
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public PriceService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;/*
        priceMap.put(String.valueOf(Cryptocurrencies.Type.SOLUSDT), 0.0);
*/

        priceMap.put("BTCUSDT", 0.0);
        priceMap.put("ETHUSDT", 0.0);
    }

    @Scheduled(fixedRate = 1000, initialDelay = 19000)
    @Transactional
    public void updatePrices() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        String formattedTime = now.format(timeFormatter);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        updatePriceForSymbol("BTCUSDT", yesterday, formattedTime, now);
        updatePriceForSymbol("ETHUSDT", yesterday, formattedTime, now);
/*
        updatePriceForSymbol(String.valueOf(Cryptocurrencies.Type.SOLUSDT), yesterday, formattedTime, now);
*/

        messagingTemplate.convertAndSend("/topic/currentPrice/BTCUSDT", priceMap.get("BTCUSDT"));
        messagingTemplate.convertAndSend("/topic/currentPrice/ETHUSDT", priceMap.get("ETHUSDT"));
/*
        messagingTemplate.convertAndSend("/topic/currentPrice/SOLUSDT", priceMap.get(String.valueOf(Cryptocurrencies.Type.SOLUSDT)));
*/


    }

    private void updatePriceForSymbol(String symbol,LocalDate yesterday, String formattedTime, LocalDateTime now) {
        Set<Candle> candles = candleRepository.findBySymbolAndDateAndFormattedTime(symbol, yesterday, formattedTime);
        if (!candles.isEmpty()) {
            Candle candle = candles.iterator().next();
            double highPrice = candle.getHighPrice();
            double lowPrice = candle.getLowPrice();
            double openPrice = candle.getOpenPrice();
            double closePrice = candle.getClosePrice();

            if (now.getSecond() == 0) {
                priceMap.put(symbol, openPrice);
            } else if (now.getSecond() == 59) {
                priceMap.put(symbol, closePrice);
            } else {
                double randomPrice = lowPrice + (highPrice - lowPrice) * random.nextDouble();
                priceMap.put(symbol, randomPrice);
            }
        }
    }

    public Double getLatestPrice(String symbol) {
        return priceMap.get(symbol);
    }
}