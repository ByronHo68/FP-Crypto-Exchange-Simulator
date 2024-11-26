package com.Ron.tradingApps.repository;

import java.util.List;
import java.util.Optional;

import com.Ron.tradingApps.model.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraderRepository extends JpaRepository<Trader, Integer> {

    Optional<Trader> findByUsername(String username);

    Trader findByEmail(String email);

    Optional<Trader> findByUserId(String userId);

    @Override
    Optional<Trader> findById(Integer id);


    List<Trader> findAll();

}
