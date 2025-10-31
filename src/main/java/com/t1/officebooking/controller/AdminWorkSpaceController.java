package com.t1.officebooking.controller;

import com.t1.officebooking.authorization.dto.UserDataResponse;
import com.t1.officebooking.authorization.security.CustomUserDetails;
import com.t1.officebooking.dto.response.BookingResponse;
import com.t1.officebooking.dto.request.CreatingLocationRequest;
import com.t1.officebooking.dto.request.CreatingSpaceRequest;
import com.t1.officebooking.dto.request.CreatingSpaceTypeRequest;
import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.Space;
import com.t1.officebooking.model.SpaceType;
import com.t1.officebooking.service.BookingService;
import com.t1.officebooking.service.LocationService;
import com.t1.officebooking.service.PendingRegistrationService;
import com.t1.officebooking.service.SpaceService;
import com.t1.officebooking.service.UserService;
import com.t1.officebooking.dto.response.PendingRegistrationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/work-space")
public class AdminWorkSpaceController {

    private final LocationService locationService;
    private final BookingService bookingService;
    private final UserService userService;
    private final SpaceService spaceService;
    private final PendingRegistrationService pendingRegistrationService;

    @PostMapping("/create-location")
    public ResponseEntity<Location> createLocation(
            @RequestBody @Valid CreatingLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.addLocation(request));
    }

    @PostMapping("/create-space")
    public ResponseEntity<Space> createSpace(
            @RequestBody @Valid CreatingSpaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(spaceService.addSpace(request));
    }

    @PostMapping("/create-spacetype")
    public ResponseEntity<SpaceType> createSpaceType(
            @RequestBody @Valid CreatingSpaceTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(spaceService.addSpaceType(request));
    }

    @GetMapping("/location/{locationId}/bookings")
    public ResponseEntity<List<BookingResponse>> getAllActiveBookingsByLocation(
            @PathVariable Long locationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok()
                .body(bookingService.findActiveBookingsByLocation(locationId));
    }

    @GetMapping("/location/{locationId}/users")
    public ResponseEntity<List<UserDataResponse>> getAllUsersByLocation(
            @PathVariable Long locationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().body(userService.findUsersByLocation(locationId));
    }

    @GetMapping("/registration-requests")
    public ResponseEntity<List<PendingRegistrationResponse>> getPendingRegistrations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long orgId = userService.findById(UUID.fromString(userDetails.getUserId())).getOrganization().getId();
        return ResponseEntity.ok(
                pendingRegistrationService.findPendingByOrganization(orgId)
                        .stream().map(PendingRegistrationResponse::from).toList()
        );
    }

    @PostMapping("/registration-requests/{id}/approve")
    public ResponseEntity<Void> approveRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String reviewer = userDetails.getUsername();
        pendingRegistrationService.approve(id, reviewer);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/registration-requests/{id}/reject")
    public ResponseEntity<Void> rejectRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String reviewer = userDetails.getUsername();
        pendingRegistrationService.reject(id, reviewer);
        return ResponseEntity.ok().build();
    }
}
