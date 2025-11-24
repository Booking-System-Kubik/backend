package com.t1.officebooking.service;

import com.t1.officebooking.dto.request.CreatingSpaceRequest;
import com.t1.officebooking.dto.request.CreatingSpaceTypeRequest;
import com.t1.officebooking.dto.request.CreatingFloorSpacesRequest;
import com.t1.officebooking.dto.request.FilteringSpacesRequest;
import com.t1.officebooking.dto.response.SpaceTypeResponse;
import com.t1.officebooking.model.Floor;
import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.Bounds;
import com.t1.officebooking.model.Space;
import com.t1.officebooking.model.SpaceType;
import com.t1.officebooking.repository.FloorRepository;
import com.t1.officebooking.repository.SpaceRepository;
import com.t1.officebooking.repository.SpaceTypeRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceTypeRepository spaceTypeRepository;
    private final SpaceRepository spaceRepository;
    private final FloorRepository floorRepository;
    private final LocationService locationService;

    public List<Space> getFilteredSpaces(FilteringSpacesRequest request) {
        if (request.getFloorNumber() == null)
            return spaceRepository
                    .findByLocationIdAndSpaceTypeId(request.getLocationId(), request.getSpaceTypeId());
        return spaceRepository
                .findByLocationIdAndSpaceTypeIdAndFloor(
                        request.getLocationId(), request.getSpaceTypeId(), request.getFloorNumber());
    }

    @Transactional
    public Space addSpace(CreatingSpaceRequest request) {
        Location location = locationService.findById(request.getLocationId());
        SpaceType spaceType = findSpaceTypeById(request.getSpaceTypeId());
        Floor floor = floorRepository.findByLocationIdAndFloorNumber(location.getId(), request.getFloorNumber())
                .orElseThrow(() -> new EntityNotFoundException("Floor does not exist for specified location and number"));
        Space space = new Space(location, spaceType, request.getCapacity(), floor);
        if (request.getX() != null && request.getY() != null && request.getWidth() != null && request.getHeight() != null) {
            space.setBounds(new Bounds(request.getX(), request.getY(), request.getWidth(), request.getHeight()));
        }
        return spaceRepository.save(space);
    }

    @Transactional
    public void addSpaceType(CreatingSpaceTypeRequest request) {
        Location location = locationService.findById(request.getLocationId());

        SpaceType spaceType = new SpaceType(
                request.getType(),
                request.getAllowedDurations(),
                location
        );

        if (spaceTypeExists(spaceType))
            throw new EntityExistsException("Provided space type already exists");
        spaceTypeRepository.save(spaceType);
    }

    public List<SpaceTypeResponse> getAllSpaceTypes(Long locationId) {
        return spaceTypeRepository.findByLocation(
                locationService.getReference(locationId))
                .stream()
                .map(s -> SpaceTypeResponse.builder()
                        .type(s.getType())
                        .id(s.getId())
                        .build())
                .toList();
    }

    public boolean spaceTypeExists(SpaceType spaceType) {
       return spaceTypeRepository.findByType(spaceType.getType()).isPresent();
    }

    public Space findSpaceById(Long id) {
        return spaceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Space must exist"));
    }

    public SpaceType findSpaceTypeById(Long id) {
        return spaceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Space Type must exist"));
    }

    @Transactional
    public List<Space> addSpacesByFloor(CreatingFloorSpacesRequest request) {
        Location location = locationService.findById(request.getLocationId());
        Floor floor = findOrCreateFloor(request.getLocationId(), request.getFloorNumber(),
                request.getPolygon());
        
        return request.getSpaces().stream()
                .map(spaceRequest -> {
                    SpaceType spaceType = findSpaceTypeById(spaceRequest.getSpaceTypeId());
                    Space space = new Space(location, spaceType, spaceRequest.getCapacity(), floor);
                    if (spaceRequest.getX() != null && spaceRequest.getY() != null 
                            && spaceRequest.getWidth() != null && spaceRequest.getHeight() != null) {
                        space.setBounds(new Bounds(spaceRequest.getX(), spaceRequest.getY(), 
                                spaceRequest.getWidth(), spaceRequest.getHeight()));
                    }
                    return spaceRepository.save(space);
                })
                .toList();
    }

    @Transactional
    public Floor findOrCreateFloor(Long locationId, Integer floorNumber, java.util.List<com.t1.officebooking.dto.request.PointRequest> polygonRequest) {
        return floorRepository.findByLocationIdAndFloorNumber(locationId, floorNumber)
                .orElseGet(() -> {
                    Location location = locationService.findById(locationId);
                    if (polygonRequest == null || polygonRequest.isEmpty()) {
                        throw new EntityNotFoundException("Floor polygon is required for new floor");
                    }
                    java.util.List<com.t1.officebooking.model.Point> polygon = polygonRequest.stream()
                            .map(p -> new com.t1.officebooking.model.Point(p.getX(), p.getY()))
                            .toList();
                    Floor floor = new Floor(location, floorNumber, polygon);
                    return floorRepository.save(floor);
                });
    }

    public List<Space> getSpacesByLocationAndFloor(Long locationId, Integer floorNumber) {
        return spaceRepository.findByLocationIdAndFloorNumber(locationId, floorNumber);
    }
}
