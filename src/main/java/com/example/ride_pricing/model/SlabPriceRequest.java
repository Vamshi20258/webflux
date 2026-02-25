package com.example.ride_pricing.model;

public class SlabPriceRequest {

    private String vehicle;
    private String serviceType;
    private Double minKm;
    private Double maxKm;
    private Double price;

    public String getVehicle() { return vehicle; }
    public String getServiceType() { return serviceType; }
    public Double getMinKm() { return minKm; }
    public Double getMaxKm() { return maxKm; }
    public Double getPrice() { return price; }
}