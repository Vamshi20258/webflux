package com.example.ride_pricing.service;

import com.example.ride_pricing.common.ApiResponse;
import com.example.ride_pricing.model.*;
import com.example.ride_pricing.repository.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

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


    public Mono<PriceViewResponse> calculatePrice(String vehicleName,
                                                  Double kms,
                                                  String serviceName) {

        return vehicleRepo.findByNameIgnoreCase(vehicleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))

                .zipWith(serviceRepo.findByNameIgnoreCase(serviceName)
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))

                .flatMap(tuple -> {

                    Vehicle vehicle = tuple.getT1();
                    ServiceType service = tuple.getT2();

                    return slabRepo
                            .findByMinKmLessThanEqualAndMaxKmGreaterThanEqual(kms, kms)
                            .next()
                            .switchIfEmpty(Mono.error(new RuntimeException("Not in the range")))

                            .flatMap(slab ->
                                    vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                                                    vehicle.getId(),
                                                    slab.getId(),
                                                    service.getId())
                                            .switchIfEmpty(Mono.error(
                                                    new RuntimeException("Pricing not configured for this vehicle/service/slab")))

                                            // Inside PricingService.java -> calculatePrice method
                                            .map(vp ->
                                                    new PriceViewResponse(
                                                            vehicle.getName(),      // vehicle name
                                                            service.getName(),      // service type
                                                            slab.getMinKm() + " KM - " + slab.getMaxKm() + " KM", // range
                                                            vp.getFinalPrice(),     // price
                                                            vehicle.getActive() ? "Active" : "Inactive" // ADD THIS: status
                                                    )
                                            )
                            );
                });
    }


    public Mono<ApiResponse<Object>> addSlab(AddSlabRequest request) {

        return serviceRepo.findByNameIgnoreCase(request.getServiceType())
                .switchIfEmpty(Mono.error(new RuntimeException("Service not found")))

                .flatMap(service ->

                        slabRepo.existsByMinKmLessThanEqualAndMaxKmGreaterThanEqual(
                                        request.getMaxKm(),
                                        request.getMinKm()
                                )

                                .flatMap(exists -> {

                                    if (exists) {
                                        return Mono.error(
                                                new RuntimeException("Overlapping slab exists")
                                        );
                                    }

                                    PricingSlab slab = new PricingSlab();
                                    slab.setMinKm(request.getMinKm());
                                    slab.setMaxKm(request.getMaxKm());
                                    slab.setBasePrice(request.getBasePrice());
                                    slab.setActive(true);

                                    return slabRepo.save(slab)

                                            .flatMap(savedSlab ->

                                                    vehicleRepo.findAll()

                                                            .flatMap(vehicle -> {

                                                                Double finalPrice =
                                                                        savedSlab.getBasePrice()
                                                                                + vehicle.getPriceIncrement();

                                                                VehiclePricing vp =
                                                                        new VehiclePricing();

                                                                vp.setVehicleId(vehicle.getId());
                                                                vp.setServiceId(service.getId());
                                                                vp.setSlabId(savedSlab.getId());
                                                                vp.setFinalPrice(finalPrice);

                                                                return vpRepo.save(vp);
                                                            })

                                                            .then(Mono.just(
                                                                    ApiResponse.success(
                                                                            201,
                                                                            "Slab created & pricing auto-generated",
                                                                            null
                                                                    )
                                                            ))
                                            );
                                })
                );
    }


    public Mono<PriceUpdateResponse> updatePrice(String vehicleName,
                                                 Double kms,
                                                 String serviceName,
                                                 Double newPrice) {

        return vehicleRepo.findByNameIgnoreCase(vehicleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found")))

                .zipWith(serviceRepo.findByNameIgnoreCase(serviceName)
                        .switchIfEmpty(Mono.error(new RuntimeException("Service not found"))))

                .flatMap(tuple -> {

                    Vehicle vehicle = tuple.getT1();
                    ServiceType service = tuple.getT2();

                    return slabRepo
                            .findByMinKmLessThanEqualAndMaxKmGreaterThanEqual(kms, kms)
                            .next()
                            .switchIfEmpty(Mono.error(new RuntimeException("Slab not found")))

                            .flatMap(slab ->
                                    vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                                                    vehicle.getId(),
                                                    slab.getId(),
                                                    service.getId())
                                            .switchIfEmpty(Mono.error(
                                                    new RuntimeException("Pricing not found")))

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
                                                        .thenReturn(
                                                                new PriceUpdateResponse(
                                                                        vehicle.getName(),
                                                                        service.getName(),
                                                                        slab.getMinKm(),
                                                                        slab.getMaxKm(),
                                                                        oldPrice,
                                                                        newPrice,
                                                                        vp.getUpdatedAt()
                                                                )
                                                        );
                                            })
                            );
                });
    }

    public Flux<PricingHistory> getAllHistory() {
        return historyRepo.findAllByOrderByChangedAtDesc();
    }

    public Flux<PriceViewResponse> getAllPrices() {
        return vpRepo.findAll() // Fetch all entries from vehicle_pricing
                .flatMap(vp ->
                        Mono.zip(
                                vehicleRepo.findById(vp.getVehicleId()),
                                serviceRepo.findById(vp.getServiceId()),
                                slabRepo.findById(vp.getSlabId())
                        ).map(tuple -> {
                            Vehicle v = tuple.getT1();
                            ServiceType s = tuple.getT2();
                            PricingSlab slab = tuple.getT3();

                            return new PriceViewResponse(
                                    v.getName(),
                                    s.getName(),
                                    slab.getMinKm() + " - " + slab.getMaxKm() + " KM",
                                    vp.getFinalPrice(),
                                    v.getActive() ? "Active" : "Inactive"
                            );
                        })
                );
    }


}