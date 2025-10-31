package com.t1.officebooking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_location", columnList = "location_id"),
        @Index(name = "idx_users_organization", columnList = "organization_id")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = UserRole.class)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    @Column(length = 100, nullable = false)
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_entity_id")
    private LegalEntity legalEntity;

    private Boolean isActive = true;

    @Column(nullable = false, length = 255)
    private String hashedPassword;

    public User(Location location, String fullName, String email,
                Set<UserRole> roles, String position, String hashedPassword) {
        this.fullName = fullName;
        this.location = location;
        this.email = email;
        this.roles = roles;
        this.position = position;
        this.hashedPassword = hashedPassword;
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }
}
