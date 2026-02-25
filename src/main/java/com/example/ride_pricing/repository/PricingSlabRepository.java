package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.PricingSlab;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PricingSlabRepository
        extends ReactiveCrudRepository<PricingSlab, Long> {

    Flux<PricingSlab> findByMinKmLessThanEqualAndMaxKmGreaterThanEqual(
            Double minKm,
            Double maxKm
    );

    // Used in addSlab() to prevent overlapping slabs
    Mono<Boolean> existsByMinKmLessThanEqualAndMaxKmGreaterThanEqual(
            Double maxKm,
            Double minKm
    );
}