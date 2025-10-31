package com.t1.officebooking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "pending_registrations", indexes = {
        @Index(name = "idx_pending_reg_email", columnList = "email"),
        @Index(name = "idx_pending_reg_org", columnList = "organization_id")
})
public class PendingRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String position;

    @Column(nullable = false, length = 255)
    private String hashedPassword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    private String reviewedBy; // user id or email of approver

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}


