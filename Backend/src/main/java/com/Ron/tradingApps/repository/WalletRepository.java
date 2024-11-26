
package com.Ron.tradingApps.repository;

import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByTraderIdAndCurrency(int traderId, String currency);

    List<Wallet> findByTraderId(int traderId);

    @Query("SELECT w FROM Wallet w JOIN FETCH w.trader WHERE w.id = :id")
    Wallet findWalletWithTrader(@Param("id") Long id);
}
