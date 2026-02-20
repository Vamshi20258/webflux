package com.example.ride_pricing.model;

import jakarta.persistence.*;

@Entity
@Table(name="pricing_slabs")
public class PricingSlab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="min_km")
    private Double minKm;

    @Column(name="max_km")
    private Double maxKm;

    @Column(name="base_price")
    private Double basePrice;

    private Boolean active = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

