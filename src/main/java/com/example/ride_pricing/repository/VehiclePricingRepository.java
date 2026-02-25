package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.VehiclePricing;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface VehiclePricingRepository
        extends ReactiveCrudRepository<VehiclePricing, Long> {

    Mono<VehiclePricing> findByVehicleIdAndSlabIdAndServiceId(
            Long vehicleId,
            Long slabId,
            Long serviceId
    );
}