package com.example.ride_pricing.model;

import java.time.LocalDateTime;

public class PriceUpdateResponse {

    private String vehicle;
    private String serviceType;
    private Double minKm;
    private Double maxKm;
    private Double oldPrice;
    private Double newPrice;
    private LocalDateTime updatedAt;

    public PriceUpdateResponse(String vehicle, String serviceType,
                               Double minKm, Double maxKm,
                               Double oldPrice, Double newPrice,
                               LocalDateTime updatedAt) {
        this.vehicle = vehicle;
        this.serviceType = serviceType;
        this.minKm = minKm;
        this.maxKm = maxKm;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.updatedAt = updatedAt;
    }

    public String getVehicle() { return vehicle; }
    public String getServiceType() { return serviceType; }
    public Double getMinKm() { return minKm; }
    public Double getMaxKm() { return maxKm; }
    public Double getOldPrice() { return oldPrice; }
    public Double getNewPrice() { return newPrice; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}