package com.t1.officebooking.controller;

import com.t1.officebooking.authorization.dto.UserDataResponse;
import com.t1.officebooking.authorization.security.CustomUserDetails;
import com.t1.officebooking.dto.request.BookingByAdminRequest;
import com.t1.officebooking.dto.response.BookingResponse;
import com.t1.officebooking.dto.request.ChangingRoleRequest;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.service.BookingService;
import com.t1.officebooking.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Validated
public class AdminController {
    private final UserService userService;
    private final BookingService bookingService;

    //TODO cancel foreign bookings, checking foreign bookings, checking reports
    // group bookings etc.  ( logic depends on admin type )

    @PostMapping("/cancel/{id}")
    public void cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        bookingService.cancelBookingByAdmin(
                id,
                UUID.fromString(userDetails.getUserId()),
                isProjectAdmin(userDetails));
    }

    @GetMapping("/users/active-bookings")
    public ResponseEntity<List<BookingResponse>> getAllActiveBookingsByUser(
            @RequestParam
            @NotBlank(message = "Email cannot be blank")
            @Email(message = "Email should be valid")
            String email,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().body(
                bookingService.getUserBookingsByAdmin(
                        email,
                        UUID.fromString(userDetails.getUserId()),
                        isProjectAdmin(userDetails),
                        true
                )
        );
    }

    @GetMapping("/users/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookingsByUser(
            @RequestParam
            @NotBlank(message = "Email cannot be blank")
            @Email(message = "Email should be valid")
            String email,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().body(
                bookingService.getUserBookingsByAdmin(
                        email,
                        UUID.fromString(userDetails.getUserId()),
                        isProjectAdmin(userDetails),
                        false
                )
        );
    }

    @GetMapping("/active-bookings")
    public ResponseEntity<List<BookingResponse>> getAllActiveBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok()
                .body(bookingService.getAllActiveBookingsByAdmin(
                        UUID.fromString(userDetails.getUserId()),
                        isProjectAdmin(userDetails)));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDataResponse>> getAllUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok().body(userService.getAllUsersByAdmin(
                UUID.fromString(userDetails.getUserId()),
                isProjectAdmin(userDetails)
        ));
    }

    @PostMapping("/assign-role")
    public void assignRole(
            @RequestBody @Valid ChangingRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.assignRole(request, isProjectAdmin(userDetails),
                UUID.fromString(userDetails.getUserId()));
    }

    @PostMapping("/revoke-role")
    public void revokeRoleFromUser(
            @RequestBody @Valid ChangingRoleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.revokeRoleFromUser(request, isProjectAdmin(userDetails),
                UUID.fromString(userDetails.getUserId()));
    }

    @PostMapping("/book")
    public ResponseEntity<BookingResponse> bookSpace(
            @RequestBody @Valid BookingByAdminRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookSpaceByAdmin(
                request,
                UUID.fromString(userDetails.getUserId()),
                isProjectAdmin(userDetails)));
    }

    @DeleteMapping("/users")
    public void removeUserFromOrganization(
            @RequestParam
            @NotBlank(message = "Email cannot be blank")
            @Email(message = "Email should be valid")
            String email,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.removeUserFromOrganization(
                email,
                isProjectAdmin(userDetails),
                UUID.fromString(userDetails.getUserId())
        );
    }

    private boolean isProjectAdmin(CustomUserDetails userDetails) {
        return !userDetails.getRoles().contains(UserRole.ROLE_ADMIN_WORKSPACE);
    }
}
