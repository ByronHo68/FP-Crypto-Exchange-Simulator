package com.Ron.tradingApps.service.data;

import com.Ron.tradingApps.model.historicalData.Candle;
import com.Ron.tradingApps.repository.CandleRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BinanceService {
    @Autowired
    private CandleRepository candleRepository;

    private final OkHttpClient client = new OkHttpClient();

    public List<Candle> fetchHistoricalData(String symbol, long startTime, long endTime) throws IOException {
        List<Candle> allCandles = new ArrayList<>();
        int requests = 6 * 2;
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
        JSONArray jsonArray = new JSONArray(jsonData);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray candleData = jsonArray.getJSONArray(i);

            long openTimeMillis = candleData.getLong(0);
            LocalDateTime openTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(openTimeMillis), ZoneId.systemDefault());
            String formattedOpenTime = openTime.format(customFormatter);

            if (!candleExists(symbol, openTime)) {
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
        }

        return candles;
    }

    private boolean candleExists(String symbol, LocalDateTime openTime) {
        return candleRepository.existsBySymbolAndOpenTime(symbol, openTime);
    }
}