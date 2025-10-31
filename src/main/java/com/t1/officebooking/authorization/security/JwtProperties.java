package com.t1.officebooking.authorization.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    @NotBlank(message = "JWT secret key must not be blank")
    private String secret;

    @Positive(message = "Access token expiration must be positive value")
    private long accessExpirationMin;

    @Positive(message = "Refresh token expiration must be positive value")
    private long refreshExpirationDays;
}
