package com.example.ride_pricing.model;

public class PriceViewResponse {
    private String vehicle;
    private String serviceType;
    private String range;
    private Double price;
    private String status;

    // Fixed constructor to support 5 arguments as used in PricingService
    public PriceViewResponse(String vehicle, String serviceType, String range, Double price, String status) {
        this.vehicle = vehicle;
        this.serviceType = serviceType;
        this.range = range;
        this.price = price;
        this.status = status;
    }

    // Default constructor for serialization
    public PriceViewResponse() {}

    // Getters for JSON serialization
    public String getVehicle() { return vehicle; }
    public String getServiceType() { return serviceType; }
    public String getRange() { return range; }
    public Double getPrice() { return price; }
    public String getStatus() { return status; }
}