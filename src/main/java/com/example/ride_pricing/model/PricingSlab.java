package com.example.ride_pricing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("pricing_slabs")
public class PricingSlab {

    @Id
    private Long id;

    private Double minKm;
    private Double maxKm;
    private Double basePrice;
    private Boolean active;

    public Long getId() {
        return id;
    }

    public Double getMinKm() {
        return minKm;
    }

    public void setMinKm(Double minKm) {
        this.minKm = minKm;
    }

    public Double getMaxKm() {
        return maxKm;
    }

    public void setMaxKm(Double maxKm) {
        this.maxKm = maxKm;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }


}