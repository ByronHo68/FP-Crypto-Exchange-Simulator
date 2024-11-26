package com.Ron.tradingApps.repository;

import com.Ron.tradingApps.model.AppsUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppsUserRepository extends JpaRepository<AppsUser, Integer> {
}