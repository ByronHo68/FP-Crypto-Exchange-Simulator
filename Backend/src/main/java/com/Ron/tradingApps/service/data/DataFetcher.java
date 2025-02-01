package com.Ron.tradingApps.service.data;

import com.Ron.tradingApps.model.historicalData.Candle;
import com.Ron.tradingApps.repository.CandleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class DataFetcher implements CommandLineRunner {

    @Autowired
    private BinanceService binanceService;

    @Autowired
    private CandleRepository candleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (isCandleTableEmpty()) {
            Arrays.stream(Cryptocurrencies.Type.values()).forEach(crypto -> {
                fetchAndStoreData(crypto.name());
            });
        } else {
            System.out.println("Candle table already has data. Skipping data fetch.");
        }
    }

    private boolean isCandleTableEmpty() {
        return candleRepository.count() == 0;
    }

    private void fetchAndStoreData(String symbol) {
        try {
            LocalDateTime startDateTime = LocalDateTime.now().minusDays(2).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endDateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

            long startTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTime = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();


            List<Candle> candles = binanceService.fetchHistoricalData(symbol, startTime, endTime);
            candleRepository.saveAll(candles);
            System.out.println("Stored " + candles.size() + " candles for " + symbol);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}