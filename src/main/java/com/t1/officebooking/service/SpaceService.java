package com.t1.officebooking.service;

import com.t1.officebooking.dto.request.CreatingSpaceRequest;
import com.t1.officebooking.dto.request.CreatingSpaceTypeRequest;
import com.t1.officebooking.dto.request.CreatingFloorSpacesRequest;
import com.t1.officebooking.dto.request.FilteringSpacesRequest;
import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.Bounds;
import com.t1.officebooking.model.Space;
import com.t1.officebooking.model.SpaceType;
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
    private final LocationService locationService;

    public List<Space> getFilteredSpaces(FilteringSpacesRequest request) {
        if (request.getFloor() == null)
            return spaceRepository
                    .findByLocationIdAndSpaceTypeId(request.getLocationId(), request.getSpaceTypeId());
        return spaceRepository
                .findByLocationIdAndSpaceTypeIdAndFloor(
                        request.getLocationId(), request.getSpaceTypeId(), request.getFloor());
    }

    @Transactional
    public Space addSpace(CreatingSpaceRequest request) {
        Location location = locationService.findById(request.getLocationId());
        SpaceType spaceType = findSpaceTypeById(request.getSpaceTypeId());
        Space space = new Space(location, spaceType, request.getCapacity(), request.getFloor());
        if (request.getX() != null && request.getY() != null && request.getWidth() != null && request.getHeight() != null) {
            space.setBounds(new Bounds(request.getX(), request.getY(), request.getWidth(), request.getHeight()));
        }
        return spaceRepository.save(space);
    }

    @Transactional
    public SpaceType addSpaceType(CreatingSpaceTypeRequest request) {
        SpaceType spaceType = new SpaceType(
                request.getType(),
                request.getAllowedDurations()
        );

        if (spaceTypeExists(spaceType))
            throw new EntityExistsException("Provided space type already exists");
        return spaceTypeRepository.save(spaceType);
    }

    public List<SpaceType> getAllSpaceTypes() {
        return spaceTypeRepository.findAll();
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
        
        return request.getSpaces().stream()
                .map(spaceRequest -> {
                    SpaceType spaceType = findSpaceTypeById(spaceRequest.getSpaceTypeId());
                    Space space = new Space(location, spaceType, spaceRequest.getCapacity(), request.getFloor());
                    if (spaceRequest.getX() != null && spaceRequest.getY() != null 
                            && spaceRequest.getWidth() != null && spaceRequest.getHeight() != null) {
                        space.setBounds(new Bounds(spaceRequest.getX(), spaceRequest.getY(), 
                                spaceRequest.getWidth(), spaceRequest.getHeight()));
                    }
                    return spaceRepository.save(space);
                })
                .toList();
    }

    public List<Space> getSpacesByLocationAndFloor(Long locationId, Integer floor) {
        return spaceRepository.findByLocationIdAndFloor(locationId, floor);
    }
}
