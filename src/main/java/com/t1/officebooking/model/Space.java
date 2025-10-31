package com.t1.officebooking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "spaces", indexes = {
        @Index(name = "idx_spaces_location_type", columnList = "location_id,space_type_id")
})
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_type_id", nullable = false)
    private SpaceType spaceType;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int floor;

    private boolean isBookable = true;

    @Embedded
    private Bounds bounds;

    public Space(Location location, SpaceType spaceType, int capacity, int floor) {
        this.location = location;
        this.spaceType = spaceType;
        this.capacity = capacity;
        this.floor = floor;
    }
}
