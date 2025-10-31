package com.t1.officebooking.authorization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
public class RevokedToken {
    @Id
    private String jti;

    @Column(nullable = false)
    private Instant revokedAt;

    public RevokedToken(String jti) {
        this.jti = jti;
        this.revokedAt = Instant.now();
    }
}
