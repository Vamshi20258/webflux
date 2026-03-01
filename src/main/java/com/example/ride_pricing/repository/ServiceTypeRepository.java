package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.ServiceType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ServiceTypeRepository extends ReactiveCrudRepository<ServiceType, Long> {

    Mono<ServiceType> findByNameIgnoreCase(String name);

}