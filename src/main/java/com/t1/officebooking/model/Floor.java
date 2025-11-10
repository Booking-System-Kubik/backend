package com.t1.officebooking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection
    @CollectionTable(name = "floor_polygon", joinColumns = @JoinColumn(name = "floor_id"))
    private List<Point> polygon = new ArrayList<>();

    public Floor(Location location, Integer floorNumber, List<Point> polygon) {
        this.location = location;
        this.floorNumber = floorNumber;
        if (polygon != null) {
            this.polygon = polygon;
        }
    }
}

