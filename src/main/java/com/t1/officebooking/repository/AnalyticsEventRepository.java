package com.t1.officebooking.repository;

import com.t1.officebooking.dto.stats.OfficeBookingStats;
import com.t1.officebooking.dto.stats.SpaceBookingStats;
import com.t1.officebooking.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    @Query("SELECT e FROM AnalyticsEvent e " +
            "JOIN FETCH e.user " +
            "JOIN FETCH e.space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "LEFT JOIN FETCH e.department " +
            "LEFT JOIN FETCH e.legalEntity " +
            "WHERE e.eventTimeStamp >= :startTime " +
            "AND e.eventTimeStamp <= :endTime")
    List<AnalyticsEvent> findAllBookingsBetweenDates(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT e FROM AnalyticsEvent e " +
            "JOIN FETCH e.user " +
            "JOIN FETCH e.space s " +
            "JOIN FETCH s.location l " +
            "JOIN FETCH s.spaceType " +
            "LEFT JOIN FETCH e.department " +
            "LEFT JOIN FETCH e.legalEntity " +
            "WHERE l.organization.id = :organizationId " +
            "AND e.eventTimeStamp >= :startTime " +
            "AND e.eventTimeStamp <= :endTime")
    List<AnalyticsEvent> findBookingsByOrganizationAndDates(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("organizationId") Long organizationId
    );

    @Query("SELECT e FROM AnalyticsEvent e " +
            "JOIN FETCH e.user " +
            "JOIN FETCH e.space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "LEFT JOIN FETCH e.department " +
            "LEFT JOIN FETCH e.legalEntity " +
            "WHERE e.location.id = :locationId " +
            "AND e.eventTimeStamp >= :startTime " +
            "AND e.eventTimeStamp <= :endTime")
    List<AnalyticsEvent> findBookingsByLocationAndDates(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("locationId") Long locationId
    );

    @Query("SELECT e.location.city as locationCity, e.location.name as locationName, " +
            "e.space.id as spaceId, COUNT(e) as bookingCount " +
            "FROM AnalyticsEvent e " +
            "WHERE e.location.id = :locationId " +
            "AND e.eventTimeStamp >= :startTime " +
            "AND e.eventTimeStamp <= :endTime " +
            "GROUP BY e.location.city, e.location.name, e.space.id")
    List<SpaceBookingStats> findSpaceBookingStatsByLocation(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("locationId") Long locationId
    );

    @Query("SELECT e.location.city as locationCity, e.location.name as locationName, " +
            "e.space.id as spaceId, COUNT(e) as bookingCount " +
            "FROM AnalyticsEvent e " +
            "JOIN e.location l " +
            "WHERE l.organization.id = :organizationId " +
            "AND e.eventTimeStamp >= :startTime " +
            "AND e.eventTimeStamp <= :endTime " +
            "GROUP BY e.location.city, e.location.name, e.space.id")
    List<SpaceBookingStats> findSpaceBookingStatsByOrganization(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("organizationId") Long organizationId
    );

    @Query("SELECT e.location.city as locationCity, e.location.name as locationName, " +
            "e.space.id as spaceId, COUNT(e) as bookingCount " +
            "FROM AnalyticsEvent e " +
            "WHERE e.eventTimeStamp >= :startTime " +
            "AND e.eventTimeStamp <= :endTime " +
            "GROUP BY e.location.city, e.location.name, e.space.id")
    List<SpaceBookingStats> findSpaceBookingStats(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT e.location.city as locationCity, e.location.name as locationName, " +
            "COUNT(e) as bookingCount " +
            "FROM AnalyticsEvent e " +
            "WHERE e.location.id = :locationId " +
            "AND e.eventTimeStamp BETWEEN :startTime AND :endTime " +
            "GROUP BY e.location.city, e.location.name")
    OfficeBookingStats findLocationBookingStats(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("locationId") Long locationId
    );

    @Query("SELECT e.location.city as locationCity, e.location.name as locationName, " +
            "COUNT(e) as bookingCount " +
            "FROM AnalyticsEvent e " +
            "JOIN e.location l " +
            "WHERE l.organization.id = :organizationId " +
            "AND e.eventTimeStamp BETWEEN :startTime AND :endTime " +
            "GROUP BY e.location.city, e.location.name")
    List<OfficeBookingStats> findAllLocationsBookingStatsByOrganization(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("organizationId") Long organizationId
    );

    @Query("SELECT e.location.city as locationCity, e.location.name as locationName, " +
            "COUNT(e) as bookingCount " +
            "FROM AnalyticsEvent e " +
            "WHERE e.eventTimeStamp BETWEEN :startTime AND :endTime " +
            "GROUP BY e.location.city, e.location.name")
    List<OfficeBookingStats> findAllLocationsBookingStats(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
