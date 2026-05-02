package com.t1.officebooking.authorization.service;

import com.t1.officebooking.authorization.dto.JwtResponse;
import com.t1.officebooking.authorization.security.JwtProperties;
import com.t1.officebooking.authorization.repository.RevokedTokenRepository;
import com.t1.officebooking.model.User;
import com.t1.officebooking.model.UserRole;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(Base64.getEncoder().encodeToString(new byte[32]));
        jwtProperties.setAccessExpirationMin(15);
        jwtProperties.setRefreshExpirationDays(7);
        jwtTokenService = new JwtTokenService(revokedTokenRepository, jwtProperties);
    }

    @Test
    void generateTokens_containsSubjectAndPassesValidation() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("person@example.com");
        user.setRoles(Set.of(UserRole.ROLE_USER));

        JwtResponse tokens = jwtTokenService.generateTokens(user);

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(jwtTokenService.validateToken(tokens.accessToken())).isTrue();
        assertThat(jwtTokenService.extractEmail(tokens.accessToken())).isEqualTo("person@example.com");
        assertThat(jwtTokenService.extractUUID(tokens.accessToken())).isEqualTo(user.getId());
    }

    @Test
    void validateToken_malformed_throws() {
        assertThatThrownBy(() -> jwtTokenService.validateToken("not-a-jwt"))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void isTokenRevoked_whenNotExpiredAndJtiInRepo_returnsTrue() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("a@b.com");
        user.setRoles(Set.of(UserRole.ROLE_USER));

        String token = jwtTokenService.generateTokens(user).accessToken();
        assertThat(jwtTokenService.extractExpiration(token).after(new Date())).isTrue();

        String jti = jwtTokenService.extractJti(token);
        when(revokedTokenRepository.existsByJti(jti)).thenReturn(true);

        assertThat(jwtTokenService.isTokenRevoked(token)).isTrue();
    }

    @Test
    void extractJti_roundTrip() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("x@y.com");
        user.setRoles(Set.of(UserRole.ROLE_USER));

        JwtResponse tokens = jwtTokenService.generateTokens(user);
        String jti = jwtTokenService.extractJti(tokens.accessToken());
        assertThat(jti).isNotBlank();
    }
}
