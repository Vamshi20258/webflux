package com.example.ride_pricing.repository;

import com.example.ride_pricing.model.PricingSlab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PricingSlabRepository extends JpaRepository<PricingSlab,Integer> {
    PricingSlab findByMinKmLessThanEqualAndMaxKmGreaterThan(Double km1, Double km2);


}

