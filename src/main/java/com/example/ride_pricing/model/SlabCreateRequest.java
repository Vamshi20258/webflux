package com.example.ride_pricing.model;

public class SlabCreateRequest {

    private Double minKm;
    private Double maxKm;
    private Double basePrice;
    private Boolean active;

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