package com.Ron.tradingApps.repository;

import com.Ron.tradingApps.model.historicalData.Candle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CandleRepository extends JpaRepository<Candle, Long> {
    List<Candle> findBySymbolAndFormattedTime(String symbol, String formattedTime);

    List<Candle> findBySymbolAndDateAndFormattedTime(String symbol, LocalDate yesterday, String formattedTime);

    List<Candle> findBySymbolAndOpenTimeBetween(String symbol, LocalDateTime startTime, LocalDateTime endTime);
    Optional<Candle> findBySymbolAndFormattedTimeAndDate(String symbol, String formattedTime, LocalDate date);

    Candle findBySymbolAndFormattedOpenTime(String symbol, String formattedOpenTime);

    List<Candle> findBySymbolAndDateAndFormattedTimeBetween(String symbol,LocalDate date, String startTime, String endTime);

    List<Candle> findBySymbolAndFormattedTimeBetween(String symbol, String startTime, String endTime);

    long countBySymbol(String symbol);

    void deleteBySymbol(String symbol);
    boolean existsBySymbolAndOpenTime(String symbol, LocalDateTime openTime);

    @Query("SELECT c.closePrice " +
            "FROM Candle c WHERE c.symbol = :symbol AND c.openTime = :openTime")
    Optional<BigDecimal> findClosePriceBySymbolAndOpenTime(
            @Param("symbol") String symbol,
            @Param("openTime") LocalDateTime openTime);
}

