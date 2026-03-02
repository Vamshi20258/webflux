package com.example.ride_pricing.service;

import com.example.ride_pricing.common.ApiResponse;
import com.example.ride_pricing.model.*;
import com.example.ride_pricing.repository.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    // =========================================================
    // 1️⃣ CALCULATE PRICE
    // =========================================================

    public Mono<PriceViewResponse> calculatePrice(String vehicle,
                                                  Double kms,
                                                  String serviceType) {

        return vehicleRepo.findByNameIgnoreCase(vehicle)
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))
                .zipWith(serviceRepo.findByNameIgnoreCase(serviceType)
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))
                .flatMap(tuple -> {

                    Vehicle v = tuple.getT1();
                    ServiceType s = tuple.getT2();

                    return slabRepo.findMatchingSlab(kms)
                            .next()
                            .switchIfEmpty(Mono.error(new RuntimeException("KMs not supported")))
                            .flatMap(slab ->
                                    vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                                                    v.getId(),
                                                    slab.getId(),
                                                    s.getId())
                                            .switchIfEmpty(Mono.error(new RuntimeException("Pricing not configured")))
                                            .map(vp ->
                                                    new PriceViewResponse(
                                                            v.getName(),
                                                            s.getName(),
                                                            slab.getMinKm() + " - " + slab.getMaxKm() + " KM",
                                                            vp.getFinalPrice(),
                                                            Boolean.TRUE.equals(slab.getActive()) ? "ACTIVE" : "EXPIRED",
                                                            vp.getCreatedAt(),
                                                            vp.getCreatedBy(),
                                                            vp.getUpdatedAt(),
                                                            vp.getUpdatedBy()
                                                    )
                                            )
                            );
                });
    }

    // =========================================================
    // 2️⃣ ADD NEW PRICING
    // =========================================================

    public Mono<ApiResponse<Object>> addNewPricing(SlabPriceRequest request) {

        return vehicleRepo.findByNameIgnoreCase(request.getVehicle())
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))
                .zipWith(serviceRepo.findByNameIgnoreCase(request.getServiceType())
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))
                .flatMap(tuple -> {

                    Vehicle vehicle = tuple.getT1();
                    ServiceType service = tuple.getT2();

                    return slabRepo.findExactSlab(request.getMinKm(), request.getMaxKm())
                            .switchIfEmpty(slabRepo.save(createNewSlab(request)))
                            .flatMap(slab ->

                                    vpRepo.existsByVehicleIdAndSlabIdAndServiceId(
                                                    vehicle.getId(),
                                                    slab.getId(),
                                                    service.getId())
                                            .flatMap(exists -> {

                                                if (exists) {
                                                    return Mono.just(
                                                            ApiResponse.failed(409,
                                                                    "Pricing already exists"));
                                                }

                                                VehiclePricing vp = new VehiclePricing();
                                                vp.setVehicleId(vehicle.getId());
                                                vp.setServiceId(service.getId());
                                                vp.setSlabId(slab.getId());
                                                vp.setFinalPrice(request.getPrice());

                                                return vpRepo.save(vp)
                                                        .map(saved ->
                                                                ApiResponse.success(
                                                                        201,
                                                                        "Pricing created successfully",
                                                                        new PriceViewResponse(
                                                                                vehicle.getName(),
                                                                                service.getName(),
                                                                                slab.getMinKm() + " - " + slab.getMaxKm() + " KM",
                                                                                saved.getFinalPrice(),
                                                                                "ACTIVE",
                                                                                saved.getCreatedAt(),
                                                                                saved.getCreatedBy(),
                                                                                saved.getUpdatedAt(),
                                                                                saved.getUpdatedBy()
                                                                        )
                                                                )
                                                        );
                                            })
                            );
                });
    }

    private PricingSlab createNewSlab(SlabPriceRequest request) {

        PricingSlab slab = new PricingSlab();
        slab.setMinKm(request.getMinKm());
        slab.setMaxKm(request.getMaxKm());
        slab.setBasePrice(request.getPrice());
        slab.setActive(true);

        // ❌ DO NOT SET createdAt / updatedAt
        return slab;
    }

    // =========================================================
    // 3️⃣ UPDATE PRICE
    // =========================================================

    public Mono<PriceUpdateResponse> updatePrice(String vehicle,
                                                 Double kms,
                                                 String serviceType,
                                                 Double newPrice) {

        return vehicleRepo.findByNameIgnoreCase(vehicle)
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))
                .zipWith(serviceRepo.findByNameIgnoreCase(serviceType)
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))
                .flatMap(tuple -> {

                    Vehicle v = tuple.getT1();
                    ServiceType s = tuple.getT2();

                    return slabRepo.findMatchingSlab(kms)
                            .next()
                            .switchIfEmpty(Mono.error(new RuntimeException("KMs out of range")))
                            .flatMap(slab ->
                                    vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                                                    v.getId(),
                                                    slab.getId(),
                                                    s.getId())
                                            .switchIfEmpty(Mono.error(new RuntimeException("Pricing not found")))
                                            .flatMap(vp -> {

                                                Double oldPrice = vp.getFinalPrice();

                                                vp.setFinalPrice(newPrice);

                                                return vpRepo.save(vp)
                                                        .thenReturn(new PriceUpdateResponse(
                                                                v.getName(),
                                                                s.getName(),
                                                                slab.getMinKm(),
                                                                slab.getMaxKm(),
                                                                oldPrice,
                                                                newPrice,
                                                                vp.getUpdatedAt()
                                                        ));
                                            })
                            );
                });
    }

    // =========================================================
    // 4️⃣ GET ALL PRICES
    // =========================================================

    public Flux<PriceViewResponse> getAllPrices() {

        return vpRepo.findAll()
                        .flatMap(vp ->
                                Mono.zip(
                                                vehicleRepo.findById(vp.getVehicleId()),
                                                serviceRepo.findById(vp.getServiceId()),
                                                slabRepo.findById(vp.getSlabId())
                                        )
                                        .map(tuple -> {

                                            Vehicle vehicle = tuple.getT1();
                                            ServiceType service = tuple.getT2();
                                            PricingSlab slab = tuple.getT3();

                                            return new PriceViewResponse(
                                                    vehicle.getName(),
                                                    service.getName(),
                                                    slab.getMinKm() + " - " + slab.getMaxKm() + " KM",
                                                    vp.getFinalPrice(),
                                                    Boolean.TRUE.equals(slab.getActive()) ? "ACTIVE" : "EXPIRED",
                                                    vp.getCreatedAt(),
                                                    vp.getCreatedBy(),
                                                    vp.getUpdatedAt(),
                                                    vp.getUpdatedBy()
                                            );
                                        })
                        );
    }

    // =========================================================
    // 5️⃣ GET HISTORY
    // =========================================================

    public Flux<PricingHistory> getAllHistory() {
        return historyRepo.findAllByOrderByChangedAtDesc();
    }

    // =========================================================
    // 6️⃣ GET ACTIVE PRICES
    // =========================================================

    public Mono<ApiResponse<Object>> getActivePricesByKmsAndService(Double kms,
                                                                    String serviceType) {

        return serviceRepo.findByNameIgnoreCase(serviceType)
                .switchIfEmpty(Mono.error(new RuntimeException("Service not found")))
                .flatMap(service ->
                        slabRepo.findMatchingSlab(kms)
                                .next()
                                .switchIfEmpty(Mono.error(
                                        new RuntimeException("KMs out of supported range")))
                                .flatMap(slab ->

                                        vpRepo.findBySlabIdAndServiceId(
                                                        slab.getId(),
                                                        service.getId())
                                                .flatMap(vp ->
                                                        vehicleRepo.findById(vp.getVehicleId())
                                                                .map(vehicle ->
                                                                        new PriceViewResponse(
                                                                                vehicle.getName(),
                                                                                service.getName(),
                                                                                slab.getMinKm() + " - " + slab.getMaxKm() + " KM",
                                                                                vp.getFinalPrice(),
                                                                                "ACTIVE",
                                                                                vp.getCreatedAt(),
                                                                                vp.getCreatedBy(),
                                                                                vp.getUpdatedAt(),
                                                                                vp.getUpdatedBy()
                                                                        )
                                                                )
                                                )
                                                .collectList()
                                                .map(list ->
                                                        ApiResponse.success(200,
                                                                "Active pricing retrieved",
                                                                list)
                                                )
                                )
                );
    }

    // =========================================================
    // 7️⃣ ADD VEHICLE
    // =========================================================

    public Mono<ApiResponse<Object>> addVehicle(String name) {

        return vehicleRepo.findByNameIgnoreCase(name)
                .flatMap(existing ->
                        Mono.just(ApiResponse.failed(409,
                                "Vehicle already available")))
                .switchIfEmpty(
                        vehicleRepo.save(new Vehicle(null, name))
                                .map(saved ->
                                        ApiResponse.success(201,
                                                "Vehicle added successfully",
                                                saved.getName()))
                );
    }
}