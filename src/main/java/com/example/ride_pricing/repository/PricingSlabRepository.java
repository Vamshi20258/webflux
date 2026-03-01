package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.PricingSlab;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PricingSlabRepository extends ReactiveCrudRepository<PricingSlab, Long> {

    // Exact match
    @Query("""
        SELECT * FROM pricing_slabs
        WHERE min_km = :minKm
        AND max_km = :maxKm
    """)
    Mono<PricingSlab> findExactSlab(@Param("minKm") Double minKm,
                                    @Param("maxKm") Double maxKm);

    // Range match (FIXED)
    @Query("""
        SELECT * FROM pricing_slabs
        WHERE min_km <= :kms
        AND max_km >= :kms
    """)
    Flux<PricingSlab> findMatchingSlab(@Param("kms") Double kms);
}