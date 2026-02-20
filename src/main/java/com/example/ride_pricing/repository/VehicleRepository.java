package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle,Integer> {
    Vehicle findByNameIgnoreCase(String name);
}

