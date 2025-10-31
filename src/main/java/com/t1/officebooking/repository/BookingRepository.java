package com.t1.officebooking.repository;

import com.t1.officebooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE " +
            "b.space.id = :spaceId AND " +
            "b.start < :end AND " +
            "b.end > :start AND " +
            "b.status != 'CANCELLED'")
    List<Booking> findActiveBookingsForPeriod(
            @Param("spaceId") Long spaceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "WHERE b.user.id = :userId " +
            "AND b.status != 'CANCELLED' " +
            "AND b.end > :currentTime")
    List<Booking> findActiveBookingsByUserId(
            @Param("userId") UUID userId,
            @Param("currentTime") LocalDateTime currentTime
    );


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "WHERE b.user.id = :userId")
    List<Booking> findAllBookingsByUserId(
            @Param("userId") UUID userId
    );


    @Query(nativeQuery = true, value = """
    WITH locked_space AS (
        SELECT id
        FROM spaces
        WHERE id = :spaceId
        FOR UPDATE
    )
    INSERT INTO bookings (
        id,
        user_id,
        space_id,
        start,
        end_time,
        booking_type,
        status,
        created_at,
        updated_at
    )
    SELECT
        nextval('bookings_id_seq'),
        :userId,
        :spaceId,
        :startTime,
        :endTime,
        :bookingType,
        :status,
        :createdAt,
        :updatedAt
    FROM locked_space
    WHERE NOT EXISTS (
         SELECT 1 FROM bookings
         WHERE space_id = :spaceId
         AND start < :endTime
         AND status != 'CANCELLED'
         AND end_time > :startTime
    )
    RETURNING id
""")
    Optional<Long> createBookingIfAvailable(
            @Param("userId") UUID userId,
            @Param("spaceId") Long spaceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("bookingType") String bookingType,
            @Param("status") String status,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "WHERE s.location.id = :locationId " +
            "AND b.user.id = :userId")
    List<Booking> findBookingsByLocationAndUser(
            @Param("locationId") Long locationId,
            @Param("userId") UUID userId
    );


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "WHERE s.location.id = :locationId " +
            "AND b.user.id = :userId " +
            "AND b.end > :currentTime")
    List<Booking> findActiveBookingsByLocationAndUser(
            @Param("locationId") Long locationId,
            @Param("userId") UUID userId,
            @Param("currentTime") LocalDateTime currentTime
    );


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "WHERE s.location.id = :locationId " +
            "AND b.end > :currentTime")
    List<Booking> findActiveBookingsByLocation(
            @Param("locationId") Long locationId,
            @Param("currentTime") LocalDateTime currentTime
    );


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "WHERE b.end > :currentTime")
    List<Booking> findActiveBookings(
            @Param("currentTime") LocalDateTime currentTime
    );

    @Query("""
    SELECT b FROM Booking b
    LEFT JOIN FETCH b.user u
    LEFT JOIN FETCH b.space s
    LEFT JOIN FETCH s.location
    LEFT JOIN FETCH s.spaceType
    LEFT JOIN FETCH u.department
    LEFT JOIN FETCH u.legalEntity
    WHERE b.id = :id
    """)
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);
}
