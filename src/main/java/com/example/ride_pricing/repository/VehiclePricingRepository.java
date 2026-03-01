package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.VehiclePricing;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface VehiclePricingRepository extends ReactiveCrudRepository<VehiclePricing, Long> {

    // Logic for calculatePrice() and updatePrice()
    Mono<VehiclePricing> findByVehicleIdAndSlabIdAndServiceId(Long vehicleId, Long slabId, Long serviceId);

    // Logic for addNewPricing() (prevents 409 conflict)
    @Query("SELECT EXISTS(SELECT 1 FROM vehicle_pricing vp " +
            "JOIN pricing_slabs ps ON vp.slab_id = ps.id " +
            "WHERE vp.vehicle_id = :vehicleId AND vp.service_id = :serviceId " +
            "AND ps.min_km = :minKm AND ps.max_km = :maxKm)")
    Mono<Boolean> existsByVehicleIdAndServiceIdAndRange(Long vehicleId, Long serviceId, Double minKm, Double maxKm);

    // Optimized JOIN for GET methods to include names and status
    @Query("SELECT vp.*, v.name AS vehicle_name, s.name AS service_name, " +
            "CONCAT(ps.min_km, ' - ', ps.max_km, ' KM') AS range " +
            "FROM vehicle_pricing vp " +
            "JOIN vehicles v ON vp.vehicle_id = v.id " +
            "JOIN services s ON vp.service_id = s.id " +
            "JOIN pricing_slabs ps ON vp.slab_id = ps.id")
    Flux<VehiclePricing> findAllDetailed();
}