package com.example.ride_pricing.service;

import com.example.ride_pricing.common.ApiResponse;
import com.example.ride_pricing.model.*;
import com.example.ride_pricing.repository.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PricingService {

    private final VehicleRepository vehicleRepo;
    private final ServiceTypeRepository serviceRepo;
    private final PricingSlabRepository slabRepo;
    private final VehiclePricingRepository vpRepo;
    private final PricingHistoryRepository historyRepo;

    public PricingService(
            VehicleRepository vehicleRepo,
            ServiceTypeRepository serviceRepo,
            PricingSlabRepository slabRepo,
            VehiclePricingRepository vpRepo,
            PricingHistoryRepository historyRepo
    ) {
        this.vehicleRepo = vehicleRepo;
        this.serviceRepo = serviceRepo;
        this.slabRepo = slabRepo;
        this.vpRepo = vpRepo;
        this.historyRepo = historyRepo;
    }

    public Mono<PriceViewResponse> calculatePrice(String vehicleName, Double kms, String serviceName) {
        return vehicleRepo.findByNameIgnoreCase(vehicleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))
                .zipWith(serviceRepo.findByNameIgnoreCase(serviceName)
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))
                .flatMap(tuple -> {
                    Vehicle v = tuple.getT1();
                    ServiceType s = tuple.getT2();

                    return slabRepo.findByMinKmLessThanEqualAndMaxKmGreaterThanEqual(kms, kms)
                            .next()
                            .switchIfEmpty(Mono.error(new RuntimeException("No pricing for this KM range")))
                            .flatMap(slab -> vpRepo.findByVehicleIdAndSlabIdAndServiceId(v.getId(), slab.getId(), s.getId())
                                    .switchIfEmpty(Mono.error(new RuntimeException("Pricing not configured for this vehicle/service/slab")))
                                    .map(vp -> new PriceViewResponse(
                                            v.getName(),
                                            s.getName(),
                                            slab.getMinKm() + " - " + slab.getMaxKm() + " KM",
                                            vp.getFinalPrice(),
                                            v.getActive() != null && v.getActive() ? "Active" : "Inactive"
                                    )));
                });
    }
    public Flux<PriceViewResponse> getAllPrices() {
        return vpRepo.findAllDetailed() // Uses JOIN query to get vehicle/service names
                .map(vp -> new PriceViewResponse(
                        vp.getVehicleName(),
                        vp.getServiceName(),
                        vp.getRange(),
                        vp.getFinalPrice(),
                        "Active"
                ));
    }

    /**
     * POST /pricing/addPrice
     * Handles UI requirement for adding a new price record with duplicate check (409 logic).
     */
    public Mono<ApiResponse<Object>> addNewPricing(SlabPriceRequest request) {
        return vehicleRepo.findByNameIgnoreCase(request.getVehicle())
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))
                .zipWith(serviceRepo.findByNameIgnoreCase(request.getServiceType())
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))
                .flatMap(tuple -> {
                    Vehicle vehicle = tuple.getT1();
                    ServiceType service = tuple.getT2();

                    // 409 Logic: Check if pricing already exists for this exact range
                    return vpRepo.existsByVehicleIdAndServiceIdAndRange(
                                    vehicle.getId(), service.getId(), request.getMinKm(), request.getMaxKm())
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.just(ApiResponse.failed(409,
                                            "Pricing already exists for Captain ID: " + vehicle.getName()));
                                }

                                PricingSlab slab = new PricingSlab();
                                slab.setMinKm(request.getMinKm());
                                slab.setMaxKm(request.getMaxKm());
                                slab.setBasePrice(request.getPrice());
                                slab.setActive(true);

                                return slabRepo.save(slab).flatMap(savedSlab -> {
                                    VehiclePricing vp = new VehiclePricing();
                                    vp.setVehicleId(vehicle.getId());
                                    vp.setServiceId(service.getId());
                                    vp.setSlabId(savedSlab.getId());
                                    vp.setFinalPrice(request.getPrice());
                                    vp.setCreatedAt(LocalDateTime.now()); // Populates the new DB column
                                    vp.setUpdatedAt(LocalDateTime.now());

                                    return vpRepo.save(vp).map(savedVp -> {
                                        // Set transient names for the response
                                        savedVp.setVehicleName(vehicle.getName());
                                        savedVp.setServiceName(service.getName());
                                        return ApiResponse.success(201, "Successfully created Pricing Details", savedVp);
                                    });
                                });
                            });
                });
    }

    /**
     * PUT /pricing/updatePrice
     * Updates existing price and logs to history table.
     */
    public Mono<PriceUpdateResponse> updatePrice(String vehicleName, Double kms, String serviceName, Double newPrice) {
        return vehicleRepo.findByNameIgnoreCase(vehicleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))
                .zipWith(serviceRepo.findByNameIgnoreCase(serviceName)
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))
                .flatMap(tuple -> {
                    Vehicle vehicle = tuple.getT1();
                    ServiceType service = tuple.getT2();

                    return slabRepo.findByMinKmLessThanEqualAndMaxKmGreaterThanEqual(kms, kms)
                            .next()
                            .switchIfEmpty(Mono.error(new RuntimeException("KMs range not found")))
                            .flatMap(slab -> vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                                            vehicle.getId(), slab.getId(), service.getId())
                                    .switchIfEmpty(Mono.error(new RuntimeException("Pricing not found")))
                                    .flatMap(vp -> {
                                        Double oldPrice = vp.getFinalPrice();

                                        PricingHistory history = new PricingHistory();
                                        history.setVehicleId(vehicle.getId());
                                        history.setSlabId(slab.getId());
                                        history.setServiceId(service.getId());
                                        history.setOldPrice(oldPrice);
                                        history.setNewPrice(newPrice);
                                        history.setChangedAt(LocalDateTime.now());

                                        vp.setFinalPrice(newPrice);
                                        vp.setUpdatedAt(LocalDateTime.now());

                                        return historyRepo.save(history)
                                                .then(vpRepo.save(vp))
                                                .thenReturn(new PriceUpdateResponse(
                                                        vehicle.getName(),
                                                        service.getName(),
                                                        slab.getMinKm(),
                                                        slab.getMaxKm(),
                                                        oldPrice,
                                                        newPrice,
                                                        vp.getUpdatedAt()
                                                ));
                                    }));
                });
    }

    public Flux<PricingHistory> getAllHistory() {
        return historyRepo.findAllByOrderByChangedAtDesc();
    }
}