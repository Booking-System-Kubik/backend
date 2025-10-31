package com.t1.officebooking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "organizations", indexes = {
        @Index(name = "idx_organizations_name", columnList = "name", unique = true)
})
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false)
    private Boolean isActive = true;

    public Organization(String name) {
        this.name = name;
        this.isActive = true;
    }
}


