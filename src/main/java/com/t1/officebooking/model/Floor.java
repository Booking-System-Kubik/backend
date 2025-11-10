package com.t1.officebooking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "floors", indexes = {
        @Index(name = "idx_floors_location_floor", columnList = "location_id,floor_number")
})
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    public Floor(Location location, Integer floorNumber, Integer width, Integer height) {
        this.location = location;
        this.floorNumber = floorNumber;
        this.width = width;
        this.height = height;
    }
}

