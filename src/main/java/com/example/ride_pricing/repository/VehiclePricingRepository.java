package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.PriceViewResponse;
import com.example.ride_pricing.model.VehiclePricing;
import com.example.ride_pricing.projection.PriceViewProjection;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VehiclePricingRepository extends ReactiveCrudRepository<VehiclePricing, Long> {

    Mono<VehiclePricing> findByVehicleIdAndSlabIdAndServiceId(
            Long vehicleId,
            Long slabId,
            Long serviceId
    );

    Mono<Boolean> existsByVehicleIdAndSlabIdAndServiceId(
            Long vehicleId,
            Long slabId,
            Long serviceId
    );

    @Query("""
    SELECT 
        v.name AS vehicle,
        s.name AS "serviceType",
        CONCAT(ps.min_km, ' - ', ps.max_km, ' KM') AS range,
        vp.final_price AS price
    FROM vehicle_pricing vp
    JOIN vehicles v ON v.id = vp.vehicle_id
    JOIN services s ON s.id = vp.service_id
    JOIN pricing_slabs ps ON ps.id = vp.slab_id
""")
    Flux<PriceViewProjection> findAllDetailed();
}