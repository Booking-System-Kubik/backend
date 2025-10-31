package com.t1.officebooking.controller;

import com.t1.officebooking.authorization.security.CustomUserDetails;
import com.t1.officebooking.dto.request.BookingRequest;
import com.t1.officebooking.dto.request.FilteringSpacesRequest;
import com.t1.officebooking.dto.request.TimeIntervalsRequest;
import com.t1.officebooking.dto.response.BookingResponse;
import com.t1.officebooking.dto.response.SpaceResponse;
import com.t1.officebooking.dto.response.TimeSlotResponse;
import com.t1.officebooking.model.Space;
import com.t1.officebooking.model.SpaceType;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.service.BookingService;
import com.t1.officebooking.service.SpaceService;
import com.t1.officebooking.service.mapper.SpaceMapperFunction;
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
@RequestMapping("/api/booking")
public class BookingController {
    private final SpaceService spaceService;
    private final BookingService bookingService;
    private final SpaceMapperFunction spaceMapper;

    @GetMapping("/types")
    public ResponseEntity<List<SpaceType>> getAllSpaceTypes() {
        return ResponseEntity.ok().body(spaceService.getAllSpaceTypes());
    }

    @PostMapping("/space-filter")
    public ResponseEntity<List<SpaceResponse>> getSpacesFilteredByTypeAndLocation(
            @RequestBody @Valid FilteringSpacesRequest request) {
        return ResponseEntity.ok(spaceService.getFilteredSpaces(request)
                .stream().map(spaceMapper).toList());
    }

    @PostMapping("/time-intervals")
    public ResponseEntity<List<TimeSlotResponse>> getTimeIntervalsBySpace(
            @RequestBody @Valid TimeIntervalsRequest request) {
        return ResponseEntity.ok()
                .body(bookingService.getDayAvailability(request));
    }

    @PostMapping("/book")
    public ResponseEntity<BookingResponse> bookSpace(@RequestBody @Valid BookingRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.bookSpace(request, userId, isUser(userDetails)));
    }

    @PostMapping("/cancel/{id}")
    public void cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        bookingService.cancelBooking(id,UUID.fromString(customUserDetails.getUserId()));
    }

    @GetMapping("/active-bookings")
    public ResponseEntity<List<BookingResponse>> getActiveBookingsByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().body(
                bookingService.getAllActiveUserBookings(UUID.fromString(userDetails.getUserId()))
        );
    }

    @GetMapping("/all-bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookingsByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().body(
                bookingService.getAllUserBookings(UUID.fromString(userDetails.getUserId()))
        );
    }

    private boolean isUser(CustomUserDetails userDetails) {
        return !userDetails.getRoles().contains(UserRole.ROLE_ADMIN_WORKSPACE) &&
                !userDetails.getRoles().contains(UserRole.ROLE_ADMIN_PROJECT);
    }
}
