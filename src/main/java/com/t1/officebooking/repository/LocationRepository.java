package com.t1.officebooking.repository;

import com.t1.officebooking.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByCityAndAddressAndOrganization_Id(String city, String address, Long organizationId);

    java.util.List<Location> findByOrganization_Id(Long organizationId);

    @Query("SELECT l FROM Location l " +
            "JOIN FETCH l.organization " +
            "WHERE l.organization.id = :organizationId")
    List<Location> findByOrganizationIdWithDetails(@Param("organizationId") Long organizationId);

    @Query("SELECT l FROM Location l " +
            "JOIN FETCH l.organization " +
            "WHERE l.id = :id")
    java.util.Optional<Location> findByIdWithDetails(@Param("id") Long id);
}
