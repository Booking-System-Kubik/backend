package com.t1.officebooking.authorization.service;

import com.t1.officebooking.authorization.security.CustomUserDetails;
import com.t1.officebooking.authorization.security.JwtProperties;
import com.t1.officebooking.authorization.dto.JwtResponse;
import com.t1.officebooking.model.User;
import com.t1.officebooking.authorization.repository.RevokedTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtProperties jwtProperties;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtResponse generateTokens(User user) {
        Instant now = Instant.now();

        String accessToken = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("roles", user.getRoles())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(jwtProperties.getAccessExpirationMin(), ChronoUnit.MINUTES)))
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(jwtProperties.getRefreshExpirationDays(), ChronoUnit.DAYS)))
                .setId(UUID.randomUUID().toString())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        return new JwtResponse(
                accessToken,
                refreshToken,
                jwtProperties.getAccessExpirationMin() * 60,
                jwtProperties.getRefreshExpirationDays() * 24 * 60 * 60
        );
    }

    public boolean isTokenRevoked(String token) {
        String jti = extractJti(token);
        Date expiration = extractExpiration(token);

        if (expiration.before(new Date())) {
            return false;
        }

        return revokedTokenRepository.existsByJti(jti);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new MalformedJwtException("Invalid JWT token", e);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new ExpiredJwtException(null, null, "Jwt token is Expired");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new UnsupportedJwtException("JWT token is unsupported: {}", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new IllegalArgumentException("JWT claims string is empty: {}", e);
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Set<String> extractRoles(String token) {
        List<String> roles = extractClaim(token, claims -> claims.get("roles", List.class));
        return roles != null ? new HashSet<>(roles) : Collections.emptySet();
    }

    public UUID extractUUID(String token) {
        return UUID.fromString(extractClaim(token, claims -> claims.get("userId", String.class)));
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = new CustomUserDetails(
                extractUUID(token).toString(),
                extractEmail(token),
                extractRoles(token).stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}
