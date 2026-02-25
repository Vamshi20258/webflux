package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.Vehicle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface VehicleRepository extends ReactiveCrudRepository<Vehicle,Long> {
    Mono<Vehicle> findByNameIgnoreCase(String name);
}

