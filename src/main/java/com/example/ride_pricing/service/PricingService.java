package com.example.ride_pricing.service;

import com.example.ride_pricing.model.*;
import com.example.ride_pricing.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PricingService {

    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private ServiceTypeRepository serviceRepo;

    @Autowired
    private PricingSlabRepository slabRepo;

    @Autowired
    private VehiclePricingRepository vpRepo;

    @Autowired
    private PricingHistoryRepository historyRepo;


    public Double calculatePrice(String vehicleName, Double kms, String serviceName) {

        Vehicle vehicle = vehicleRepo.findByNameIgnoreCase(vehicleName);
        if (vehicle == null)
            throw new RuntimeException("Vehicle not found: " + vehicleName);

        ServiceType service = serviceRepo.findByNameIgnoreCase(serviceName);
        if (service == null)
            throw new RuntimeException("Service not found: " + serviceName);


        PricingSlab slab =
                slabRepo.findByMinKmLessThanEqualAndMaxKmGreaterThan(kms, kms);

        if (slab == null)
            throw new RuntimeException("No pricing slab found for kms: " + kms);

        VehiclePricing vp =
                vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                        vehicle.getId(), slab.getId(), service.getId());

        if (vp == null)
            throw new RuntimeException("Pricing not configured for this combination");

        return vp.getFinalPrice();
    }


    public String updatePrice(String vehicleName,
                              Double kms,
                              String serviceName,
                              Double newPrice) {

        Vehicle vehicle = vehicleRepo.findByNameIgnoreCase(vehicleName);
        if (vehicle == null)
            throw new RuntimeException("Vehicle not found: " + vehicleName);

        ServiceType service = serviceRepo.findByNameIgnoreCase(serviceName);
        if (service == null)
            throw new RuntimeException("Service not found: " + serviceName);


        PricingSlab slab =
                slabRepo.findByMinKmLessThanEqualAndMaxKmGreaterThan(kms, kms);

        if (slab == null)
            throw new RuntimeException("No slab found for kms: " + kms);

        VehiclePricing vp =
                vpRepo.findByVehicleIdAndSlabIdAndServiceId(
                        vehicle.getId(), slab.getId(), service.getId());

        if (vp == null)
            throw new RuntimeException("Pricing row missing!");


        PricingHistory history = new PricingHistory();
        history.setVehicleId(vehicle.getId());
        history.setSlabId(slab.getId());
        history.setServiceId(service.getId());
        history.setOldPrice(vp.getFinalPrice());
        history.setNewPrice(newPrice);
        history.setChangedAt(LocalDateTime.now());

        historyRepo.save(history);


        vp.setFinalPrice(newPrice);
        vp.setUpdatedAt(LocalDateTime.now());
        vpRepo.save(vp);

        return "Price updated successfully to :"+newPrice;
    }


    public List<PricingHistory> getAllHistory()
    {
        return historyRepo.findAllByOrderByChangedAtDesc();
    }
}
