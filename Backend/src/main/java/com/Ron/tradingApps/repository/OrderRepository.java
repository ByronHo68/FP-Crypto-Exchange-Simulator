package com.Ron.tradingApps.repository;

import com.Ron.tradingApps.model.Order;
import com.Ron.tradingApps.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Wallet> findByTraderId(int traderId);
    Optional<Wallet> findByCurrency(String currency);
    Optional<Wallet> findByTraderIdAndCurrency(int traderId, String currency);

    List<Order> findAllByTraderId(int traderId);

    @Query("SELECT o FROM Order o WHERE o.trader.id = :traderId AND o.orderStatus = 'PENDING'")
    List<Order> findPendingOrdersByTraderId(@Param("traderId") int traderId);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PENDING'")
    List<Order> findAllPendingOrders();
}