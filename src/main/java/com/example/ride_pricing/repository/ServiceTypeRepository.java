package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTypeRepository extends JpaRepository<ServiceType,Integer> {
    ServiceType findByNameIgnoreCase(String name);
}

