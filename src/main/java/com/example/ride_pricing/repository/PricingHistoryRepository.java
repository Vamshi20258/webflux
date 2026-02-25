package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.PricingHistory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PricingHistoryRepository extends ReactiveCrudRepository<PricingHistory, Long> {

    Flux<PricingHistory> findAllByOrderByChangedAtDesc();
}
