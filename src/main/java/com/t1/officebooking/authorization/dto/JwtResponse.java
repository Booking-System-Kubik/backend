package com.t1.officebooking.authorization.dto;

public record JwtResponse (
        String accessToken,
        String refreshToken,
        long expiresIn,
        long refreshExpiresIn
) {};
