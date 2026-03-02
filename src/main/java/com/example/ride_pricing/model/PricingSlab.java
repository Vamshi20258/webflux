package com.example.ride_pricing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("pricing_slabs")
public class PricingSlab extends AuditableBase {

    @Id
    private Long id;

    @Column("min_km")
    private Double minKm;

    @Column("max_km")
    private Double maxKm;

    @Column("base_price")
    private Double basePrice;

    @Column("active")
    private Boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}