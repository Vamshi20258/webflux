package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.VehiclePricing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiclePricingRepository extends JpaRepository<VehiclePricing,Integer> {
    VehiclePricing findByVehicleIdAndSlabIdAndServiceId(Integer vehicleId, Integer slabId,Integer ServiceId);
}

