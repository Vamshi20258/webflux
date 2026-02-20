package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.PricingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PricingHistoryRepository extends JpaRepository<PricingHistory,Integer> {
    List<PricingHistory> findAllByOrderByChangedAtDesc();
}
