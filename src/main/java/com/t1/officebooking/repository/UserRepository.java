package com.t1.officebooking.repository;


import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail (String email);

    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.location " +
            "WHERE u.location.id = :locationId")
    List<User> findByLocation(@Param("locationId") Long locationId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.location")
    List<User> findAllWithDetails();

    @Query("SELECT u FROM User u " +
            "WHERE u.organization.id = :organizationId")
    List<User> findByOrganization(@Param("organizationId") Long organizationId);
}
