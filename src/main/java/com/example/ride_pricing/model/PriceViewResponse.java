package com.example.ride_pricing.model;

import java.time.LocalDateTime;

public class PriceViewResponse {

    private String vehicle;
    private String serviceType;
    private String range;
    private Double price;
    private String status;
    private LocalDateTime createdAt;

    public PriceViewResponse() {
    }

    public PriceViewResponse(String vehicle, String serviceType, String range, Double price, String status, LocalDateTime createdAt) {
        this.vehicle = vehicle;
        this.serviceType = serviceType;
        this.range = range;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getVehicle() { return vehicle; }
    public void setVehicle(String vehicle) { this.vehicle = vehicle; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}