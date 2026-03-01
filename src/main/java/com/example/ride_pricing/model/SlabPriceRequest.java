package com.example.ride_pricing.model;

public class SlabPriceRequest {

    private String vehicle;
    private String serviceType;
    private Double minKm;
    private Double maxKm;
    private Double price;

    public String getVehicle() { return vehicle; }
    public void setVehicle(String vehicle) { this.vehicle = vehicle; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public Double getMinKm() { return minKm; }
    public void setMinKm(Double minKm) { this.minKm = minKm; }

    public Double getMaxKm() { return maxKm; }
    public void setMaxKm(Double maxKm) { this.maxKm = maxKm; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}