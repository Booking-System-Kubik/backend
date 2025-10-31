package com.t1.officebooking.authorization.controller;

import com.t1.officebooking.authorization.dto.*;
import com.t1.officebooking.authorization.security.CustomUserDetails;
import com.t1.officebooking.authorization.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegistrationRequest request) {
        boolean pending = authService.register(request);
        return pending ? ResponseEntity.accepted().build() : ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public void logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.revokeToken(request.getRefreshToken());
    }

    @GetMapping("/check-auth")
    public UserDataResponse checkAuth(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return authService.checkAuth(UUID.fromString(userDetails.getUserId()));
    }
}
