package com.Ron.tradingApps.repository;

import com.Ron.tradingApps.model.Instructor;
import com.Ron.tradingApps.model.Trader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Integer>{

   Optional<Instructor> findByUsername(String username);
}
