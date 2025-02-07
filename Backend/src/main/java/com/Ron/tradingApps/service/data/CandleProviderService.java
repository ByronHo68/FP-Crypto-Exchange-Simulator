package com.Ron.tradingApps.service.data;

import com.Ron.tradingApps.dto.CandleDTO;
import com.Ron.tradingApps.model.Cryptocurrencies;
import com.Ron.tradingApps.model.historicalData.Candle;
import com.Ron.tradingApps.repository.CandleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CandleProviderService {
    private final Map<String, List<CandleDTO>> candlesBySymbol = new ConcurrentHashMap<>();

    @Autowired
    private CandleRepository candleRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private int lastUpdatedMinute = -1;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void updateCandlesInBatch(List<CandleDTO> candleDTOs) {
        Set<CandleDTO> uniqueCandles = new HashSet<>(candleDTOs);
        uniqueCandles.forEach(dto -> updateCandlesBySymbol(dto.getSymbol(), dto));
    }

    public void updateCandlesBySymbol(String symbol, CandleDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getSymbol())) {
            return;
        }

        List<CandleDTO> existingCandles = candlesBySymbol.computeIfAbsent(symbol, k -> new ArrayList<>());

        if (!existingCandles.contains(dto)) {
            existingCandles.add(dto);
        }
    }

    public List<CandleDTO> getCandlesBySymbol(String symbol) {
        return candlesBySymbol.getOrDefault(symbol, List.of());
    }

    @Scheduled(fixedRate = 1000, initialDelay = 19000)
    public void scheduledFetchCandles() {

        LocalDateTime now = LocalDateTime.now();
        int currentMinute = now.getMinute();

        if (currentMinute != lastUpdatedMinute) {
            messagingTemplate.convertAndSend("/topic/candles/BTCUSDT", fetchLeastCandles("BTCUSDT"));
            messagingTemplate.convertAndSend("/topic/candles/ETHUSDT", fetchLeastCandles("ETHUSDT"));
/*
            messagingTemplate.convertAndSend("/topic/candles/SOLUSDT", fetchLeastCandles(String.valueOf(Cryptocurrencies.Type.SOLUSDT)));
*/

            lastUpdatedMinute = currentMinute;
        }
    }

    public CandleDTO fetchLeastCandles(String symbol) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now().minusDays(1).minusMinutes(1);
        String nowS = now.format(formatter);

        Candle candle = candleRepository.findBySymbolAndFormattedOpenTime(symbol, nowS);


        if (candle == null) {
            log.warn("No candle found for symbol: {} at time: {}", symbol, nowS);
            return null;
        }

        CandleDTO candleDTO = new CandleDTO(
                candle.getSymbol(),
                candle.getOpenTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                candle.getOpenPrice(),
                candle.getHighPrice(),
                candle.getLowPrice(),
                candle.getClosePrice(),
                candle.getVolume(),
                candle.getFormattedTime(),
                candle.getDate(),
                candle.getFormattedOpenTime()
        );

        return candleDTO;
    }

    public List<CandleDTO> fetchCandlesByTime(String symbol) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = now.minusDays(1).minusMinutes(121);
        LocalDateTime endDateTime = now.minusDays(1).minusMinutes(1);

        log.info("Fetching candles from {} to {}", startDateTime, endDateTime);

        List<Candle> candles = candleRepository.findBySymbolAndOpenTimeBetween(symbol, startDateTime, endDateTime);

        log.info("Candles fetched: {}", candles.size());

        List<CandleDTO> candleDTOs = candles.stream()
                .map(candle -> new CandleDTO(
                        candle.getSymbol(),
                        candle.getOpenTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        candle.getOpenPrice(),
                        candle.getHighPrice(),
                        candle.getLowPrice(),
                        candle.getClosePrice(),
                        candle.getVolume(),
                        candle.getFormattedTime(),
                        candle.getDate(),
                        candle.getFormattedOpenTime()))
                .collect(Collectors.toList());

        updateCandlesInBatch(candleDTOs);

        return candleDTOs;
    }
}