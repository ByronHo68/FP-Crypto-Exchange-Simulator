package com.Ron.tradingApps.service.data;

import com.Ron.tradingApps.model.historicalData.Candle;
import com.Ron.tradingApps.repository.CandleRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@EnableScheduling
public class ScheduleBinanceService {

    @Autowired
    private CandleRepository candleRepository;

    @Scheduled(cron = "26 1 0 * * *", zone = "Asia/Hong_Kong")
    public void run() throws Exception {
        fetchAndStoreData("BTCUSDT");
        fetchAndStoreData("ETHUSDT");
    }

    private void fetchAndStoreData(String symbol) {
        try {
            LocalDateTime startDateTime = LocalDateTime.now().minusDays(1).withHour(0).withMinute(1).withSecond(0);
            LocalDateTime endDateTime = LocalDateTime.now().withHour(0).minusMinutes(1).withSecond(0);

            long startTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTime = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            List<Candle> candles = fetchHistoricalData(symbol, startTime, endTime);

            int attempts = 0;
            while (candles.size() < 1440 && attempts < 12) {
                log.warn("Only {} candles retrieved for {}. Attempting to fetch missing candles (Attempt {}/{})", candles.size(), symbol, attempts + 1, 3);
                TimeUnit.SECONDS.sleep(10);
                fetchMissingCandle(symbol, startTime, endTime);
                candles = fetchHistoricalData(symbol, startTime, endTime);
                attempts++;
            }
            candleRepository.saveAll(candles);
            log.info("Stored {} candles for {}", candles.size(), symbol);

        } catch (IOException | InterruptedException e) {
            log.error("Error fetching data for {}: {}", symbol, e.getMessage());
        }
    }

    private void fetchMissingCandle(String symbol, long startTime, long endTime) {
        try {
            List<Candle> missingCandles = fetchHistoricalData(symbol, startTime, endTime);
            if (!missingCandles.isEmpty()) {
                candleRepository.saveAll(missingCandles);
                log.info("Stored additional {} missing candles for {}", missingCandles.size(), symbol);
            } else {
                log.warn("No missing candles found for {}", symbol);
            }
        } catch (IOException e) {
            log.error("Error fetching missing candles for {}: {}", symbol, e.getMessage());
        }
    }

    private final OkHttpClient client = new OkHttpClient();

    public List<Candle> fetchHistoricalData(String symbol, long startTime, long endTime) throws IOException {
        List<Candle> allCandles = new ArrayList<>();
        int requests = 6;
        long intervalDuration = (endTime - startTime) / requests;

        for (int i = 0; i < requests; i++) {
            long reqStartTime = startTime + i * intervalDuration;
            long reqEndTime = reqStartTime + intervalDuration;

            String url = String.format("https://api.binance.com/api/v3/klines?symbol=%s&interval=1m&startTime=%d&endTime=%d&limit=500",
                    symbol, reqStartTime, reqEndTime);

            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseData = response.body().string();
                allCandles.addAll(parseCandleData(responseData, symbol));
            }
        }

        return allCandles;
    }

    private List<Candle> parseCandleData(String jsonData, String symbol) {
        List<Candle> candles = new ArrayList<>();
        Set<Long> existingOpenTimes = new HashSet<>();
        JSONArray jsonArray = new JSONArray(jsonData);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray candleData = jsonArray.getJSONArray(i);

            long openTimeMillis = candleData.getLong(0);

            if (!existingOpenTimes.add(openTimeMillis)) {
                continue;
            }

            LocalDateTime openTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(openTimeMillis), ZoneId.systemDefault());
            String formattedOpenTime = openTime.format(customFormatter);

            Candle candle = new Candle();
            candle.setSymbol(symbol);
            candle.setOpenTime(openTime);
            candle.setFormattedOpenTime(formattedOpenTime);
            candle.setOpenPrice(candleData.getDouble(1));
            candle.setHighPrice(candleData.getDouble(2));
            candle.setLowPrice(candleData.getDouble(3));
            candle.setClosePrice(candleData.getDouble(4));
            candle.setVolume(candleData.getDouble(5));

            candle.setFormattedTime(openTime.format(timeFormatter));
            candle.setDate(openTime.toLocalDate());

            candles.add(candle);
        }

        return candles;
    }
}