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
                            .switchIfEmpty(Mono.error(new RuntimeException("Slab not found")))

                            .flatMap(slab ->
                                    vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                                                    vehicle.getId(),
                                                    slab.getId(),
                                                    service.getId())
                                            .switchIfEmpty(Mono.error(
                                                    new RuntimeException("Pricing not configured for this vehicle/service/slab")))

                                            .map(vp ->
                                                    new PriceViewResponse(
                                                            vehicle.getName(),
                                                            service.getName(),
                                                            slab.getMinKm() + " KM - "
                                                                    + slab.getMaxKm() + " KM",
                                                            vp.getFinalPrice()
                                                    )
                                            )
                            );
                });
    }


    public Mono<ApiResponse<Object>> addSlab(AddSlabRequest request) {
        // 1. Check for overlapping slabs to maintain data integrity
        return slabRepo.existsByMinKmLessThanEqualAndMaxKmGreaterThanEqual(
                        request.getMaxKm(),
                        request.getMinKm()
                )
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Overlapping slab exists"));
                    }

                    PricingSlab slab = new PricingSlab();
                    slab.setMinKm(request.getMinKm());
                    slab.setMaxKm(request.getMaxKm());
                    slab.setBasePrice(request.getBasePrice());
                    slab.setActive(true);

                    // 2. Save the distance slab first
                    return slabRepo.save(slab)
                            .flatMap(savedSlab ->
                                    // 3. Fetch ALL available services (Ride, Parcel, Scheduled)
                                    serviceRepo.findAll()
                                            .flatMap(service ->
                                                    // 4. Fetch ALL vehicles (Auto, Mini, Prime, XUV, Bike)
                                                    vehicleRepo.findAll()
                                                            .flatMap(vehicle -> {
                                                                // 5. Apply your tiered increment logic (+100, +200, etc.)
                                                                Double finalPrice = calculateDynamicPrice(
                                                                        vehicle.getName(),
                                                                        savedSlab.getMinKm(),
                                                                        savedSlab.getBasePrice()
                                                                );

                                                                // 6. Mandatory Requirement: Half price for Parcel service
                                                                if ("parcel".equalsIgnoreCase(service.getName())) {
                                                                    finalPrice = finalPrice / 2;
                                                                }

                                                                // 7. Map to your VehiclePricing model
                                                                VehiclePricing vp = new VehiclePricing();
                                                                vp.setVehicleId(vehicle.getId());
                                                                vp.setServiceId(service.getId());
                                                                vp.setSlabId(savedSlab.getId());
                                                                vp.setFinalPrice(finalPrice);
                                                                vp.setUpdatedAt(LocalDateTime.now());

                                                                return vpRepo.save(vp);
                                                            })
                                            )
                                            .then(Mono.just(
                                                    ApiResponse.success(201, "Slab created and full Cartesian product generated", null)
                                            ))
                            );
                });
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


    private Double calculateDynamicPrice(String vehicleName, Double minKm, Double basePrice) {
        String name = vehicleName.toLowerCase();

        // Bike and EV Bike use the base price directly as the foundation
        if (name.contains("bike")) {
            return basePrice;
        }

        double increment = 0.0;

        // Range-specific increments for Auto, Mini, Prime, and XUV
        if (minKm >= 100 && minKm < 125) {
            if (name.contains("auto")) increment = 100.0;
            else if (name.contains("mini")) increment = 200.0;
            else if (name.contains("prime")) increment = 300.0;
            else if (name.contains("xuv")) increment = 400.0;
        }
        else if (minKm >= 125 && minKm < 150) {
            if (name.contains("auto")) increment = 125.0;
            else if (name.contains("mini")) increment = 250.0;
            else if (name.contains("prime")) increment = 375.0;
            else if (name.contains("xuv")) increment = 500.0;
        }
        else if (minKm >= 150 && minKm < 200) {
            if (name.contains("auto")) increment = 150.0;
            else if (name.contains("mini")) increment = 300.0;
            else if (name.contains("prime")) increment = 450.0;
            else if (name.contains("xuv")) increment = 600.0;
        }
        else if (minKm >= 200 && minKm < 300) {
            if (name.contains("auto")) increment = 200.0;
            else if (name.contains("mini")) increment = 400.0;
            else if (name.contains("prime")) increment = 600.0;
            else if (name.contains("xuv")) increment = 800.0;
        }
        else if (minKm >= 300 && minKm < 400) {
            if (name.contains("auto")) increment = 300.0;
            else if (name.contains("mini")) increment = 600.0;
            else if (name.contains("prime")) increment = 900.0;
            else if (name.contains("xuv")) increment = 1200.0;
        }
        else if (minKm >= 400 && minKm <= 500) {
            // Based on your 400 base price request
            if (name.contains("auto")) increment = 50.0;
            else if (name.contains("mini")) increment = 100.0;
            else if (name.contains("prime")) increment = 150.0;
            else if (name.contains("xuv")) increment = 200.0;
        }

        return basePrice + increment;
    }
}