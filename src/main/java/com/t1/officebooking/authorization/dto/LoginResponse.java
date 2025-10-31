package com.t1.officebooking.authorization.dto;

import com.t1.officebooking.model.UserRole;

import java.util.Set;

public record LoginResponse(
        JwtResponse jwtResponse,
        Set<UserRole> role)
{};
