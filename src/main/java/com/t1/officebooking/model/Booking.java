package com.t1.officebooking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_space_time",
                columnList = "space_id, start, end_time"),

        @Index(name = "idx_bookings_user_id",
                columnList = "user_id"),

        @Index(name = "idx_bookings_end",
                columnList = "end_time")

})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookings_seq")
    @SequenceGenerator(
            name = "bookings_seq",
            sequenceName = "bookings_id_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(nullable = false)
    private LocalDateTime start;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime end;

    @Column(nullable = false, length = 20)
    private String bookingType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 255)
    private String recurringRule;
}
