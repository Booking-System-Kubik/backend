package com.t1.officebooking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private LocalTime workDayStart;

    @Column(nullable = false)
    private LocalTime workDayEnd;

    @Column(nullable = false)
    private String timeZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public Location(String name, String city, String address, Boolean isActive,
                    LocalTime workDayStart, LocalTime workDayEnd, String timezone) {
        this.name = name;
        this.city = city;
        this.isActive = isActive;
        this.address = address;
        this.workDayStart = workDayStart;
        this.workDayEnd = workDayEnd;
        this.timeZone = timezone;
    }
}
