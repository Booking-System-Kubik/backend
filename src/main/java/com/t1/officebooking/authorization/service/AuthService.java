package com.t1.officebooking.authorization.service;

import com.t1.officebooking.authorization.dto.*;
import com.t1.officebooking.authorization.model.RevokedToken;
import com.t1.officebooking.authorization.repository.RevokedTokenRepository;
import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.Organization;
import com.t1.officebooking.model.User;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.service.LocationService;
import com.t1.officebooking.service.OrganizationService;
import com.t1.officebooking.service.PendingRegistrationService;
import com.t1.officebooking.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final LocationService locationService;
    private final OrganizationService organizationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PendingRegistrationService pendingRegistrationService;

    @Transactional
    public boolean register(RegistrationRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }

        Location location = request.getLocation() != null
                ? locationService.findById(request.getLocation())
                :null;

        Organization organization = null;
        boolean createdNewOrganization = false;
        if (request.getOrganizationId() != null) {
            organization = organizationService.findById(request.getOrganizationId());
        } else if (request.getOrganizationName() != null && !request.getOrganizationName().isBlank()) {
            organization = organizationService.create(request.getOrganizationName().trim());
            createdNewOrganization = true;
        }

        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.ROLE_USER);
        if (createdNewOrganization) {
            roles.add(UserRole.ROLE_ADMIN_WORKSPACE);
        }

        // Existing organization chosen: create pending registration, do not create user
        if (!createdNewOrganization && organization != null) {
            pendingRegistrationService.create(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPosition(),
                    passwordEncoder.encode(request.getPassword()),
                    organization.getId(),
                    request.getLocation()
            );
            return true; // pending created
        }

        // New organization: create user immediately
        User user = new User(
                location,
                request.getFullName(),
                request.getEmail(),
                roles,
                request.getPosition(),
                passwordEncoder.encode(request.getPassword())
        );
        user.setOrganization(organization);
        userService.saveUser(user);
        return false; // not pending
    }

    public LoginResponse authenticate(LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return new LoginResponse(jwtTokenService.generateTokens(user), user.getRoles());
    }

    public JwtResponse refreshToken(String refreshToken) {
        jwtTokenService.validateToken(refreshToken);

        if (jwtTokenService.isTokenRevoked(refreshToken)) {
            throw new ExpiredJwtException(null, null, "Token has been revoked");
        }

        String email = jwtTokenService.extractEmail(refreshToken);

        User user = userService.findByEmail(email);

        return jwtTokenService.generateTokens(user);
    }

    @Transactional
    public void revokeToken(String token) {
        String jti = jwtTokenService.extractJti(token);
        revokedTokenRepository.save(new RevokedToken(jti));
    }

    public UserDataResponse checkAuth(UUID userId) {
        return userService.getUserData(userId);
    }
}
