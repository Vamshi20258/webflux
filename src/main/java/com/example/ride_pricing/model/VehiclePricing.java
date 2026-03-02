package com.example.ride_pricing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("vehicle_pricing")
public class VehiclePricing extends AuditableBase {
    @Id
    private Long id;
    private Long vehicleId;
    private Long slabId;
    private Long serviceId;
    private Double finalPrice;

    public VehiclePricing() {
    }

    public VehiclePricing(Long id, Long vehicleId, Long slabId, Long serviceId, Double finalPrice) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.slabId = slabId;
        this.serviceId = serviceId;
        this.finalPrice = finalPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getSlabId() {
        return slabId;
    }

    public void setSlabId(Long slabId) {
        this.slabId = slabId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }
}