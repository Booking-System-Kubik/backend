package com.t1.officebooking.repository;

import com.t1.officebooking.model.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {
    @Query("SELECT f FROM Floor f " +
            "JOIN FETCH f.location " +
            "WHERE f.location.id = :locationId " +
            "AND f.floorNumber = :floorNumber")
    Optional<Floor> findByLocationIdAndFloorNumber(
            @Param("locationId") Long locationId,
            @Param("floorNumber") Integer floorNumber);
}

