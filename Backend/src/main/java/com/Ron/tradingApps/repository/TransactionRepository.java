
package com.Ron.tradingApps.repository;

import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}