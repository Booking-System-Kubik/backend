package com.t1.officebooking.repository;

import com.t1.officebooking.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpaceRepository extends JpaRepository<Space, Long> {
    @Query("SELECT s FROM Space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "JOIN FETCH s.floor " +
            "WHERE s.location.id = :locationId " +
            "AND s.spaceType.id = :spaceTypeId")
    List<Space> findByLocationIdAndSpaceTypeId(
            @Param("locationId") Long locationId,
            @Param("spaceTypeId") Long spaceTypeId);

    @Query("SELECT s FROM Space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "JOIN FETCH s.floor " +
            "WHERE s.location.id = :locationId " +
            "AND s.spaceType.id = :spaceTypeId " +
            "AND s.floor.floorNumber = :floorNumber")
    List<Space> findByLocationIdAndSpaceTypeIdAndFloor(
            @Param("locationId") Long locationId,
            @Param("spaceTypeId") Long spaceTypeId,
            @Param("floorNumber") Integer floorNumber);

    @Query("SELECT s FROM Space s " +
            "JOIN FETCH s.location " +
            "JOIN FETCH s.spaceType " +
            "JOIN FETCH s.floor " +
            "WHERE s.location.id = :locationId " +
            "AND s.floor.floorNumber = :floorNumber")
    List<Space> findByLocationIdAndFloorNumber(
            @Param("locationId") Long locationId,
            @Param("floorNumber") Integer floorNumber);
}
