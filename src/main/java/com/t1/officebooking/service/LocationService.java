package com.t1.officebooking.service;

import com.t1.officebooking.dto.request.CreatingLocationRequest;
import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.Organization;
import com.t1.officebooking.repository.LocationRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final OrganizationService organizationService;

    @Transactional
    public Location addLocation(CreatingLocationRequest request) {

        Location location = new Location(
                request.getName(),
                request.getCity(),
                request.getAddress(),
                request.getIsActive(),
                request.getWorkDayStart(),
                request.getWorkDayEnd(),
                request.getTimeZone()
        );

        Organization organization = organizationService.findById(request.getOrganizationId());
        location.setOrganization(organization);

        if (locationRepository.existsByCityAndAddressAndOrganization_Id(
                location.getCity(), location.getAddress(), organization.getId())) {
            throw new EntityExistsException("Provided location already exists");
        }
        return locationRepository.save(location);
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Transactional
    public List<Location> getLocationsByOrganization(Long organizationId) {
        return locationRepository.findByOrganizationIdWithDetails(organizationId);
    }

    public Location findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location must exist"));
    }

    public Boolean locationExists(Long locationId) {
        return locationRepository.findById(locationId).isPresent();
    }
}
