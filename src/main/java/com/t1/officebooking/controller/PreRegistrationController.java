package com.t1.officebooking.controller;

import com.t1.officebooking.dto.response.FloorSpacesResponse;
import com.t1.officebooking.dto.response.SpaceResponse;
import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.Organization;
import com.t1.officebooking.model.SpaceType;
import com.t1.officebooking.service.LocationService;
import com.t1.officebooking.service.OrganizationService;
import com.t1.officebooking.service.SpaceService;
import com.t1.officebooking.service.mapper.SpaceMapperFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PreRegistrationController {
    private final LocationService locationService;
    private final OrganizationService organizationService;
    private final SpaceService spaceService;
    private final SpaceMapperFunction spaceMapper;

    // Get all organizations
    @GetMapping("/organizations")
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok().body(organizationService.getAll());
    }

    // Get locations by organization id
    @GetMapping("/organizations/{organizationId}/locations")
    public ResponseEntity<List<Location>> getLocationsByOrganization(
            @PathVariable Long organizationId) {
        return ResponseEntity.ok().body(locationService.getLocationsByOrganization(organizationId));
    }

    // Get spaces by location and floor
    @GetMapping("/locations/{locationId}/spaces")
    public ResponseEntity<FloorSpacesResponse> getSpacesByLocationAndFloor(
            @PathVariable Long locationId,
            @RequestParam Integer floorNumber) {
        List<SpaceResponse> spaces = spaceService.getSpacesByLocationAndFloor(locationId, floorNumber)
                .stream()
                .map(spaceMapper)
                .toList();
        
        FloorSpacesResponse.FloorResponse floorResponse = null;
        if (!spaces.isEmpty() && spaces.get(0).floor() != null) {
            SpaceResponse.FloorResponse firstFloor = spaces.get(0).floor();
            floorResponse = FloorSpacesResponse.FloorResponse.builder()
                    .id(firstFloor.id())
                    .floorNumber(firstFloor.floorNumber())
                    .polygon(firstFloor.polygon())
                    .build();
        }
        
        return ResponseEntity.ok().body(
                FloorSpacesResponse.builder()
                        .floor(floorResponse)
                        .spaces(spaces)
                        .build()
        );
    }

    @GetMapping("/locations/{locationId}/spacetypes")
    public ResponseEntity<?> getAllSpaceTypes(
            @PathVariable Long locationId
    ) {
        return ResponseEntity.ok().body(spaceService.getAllSpaceTypes(locationId));
    }
}
