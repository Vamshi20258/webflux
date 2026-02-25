package com.example.ride_pricing.model;

public class AddSlabRequest {

    private String serviceType;
    private Double minKm;
    private Double maxKm;
    private Double basePrice;

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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
}