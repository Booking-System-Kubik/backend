package com.t1.officebooking.repository;

import com.t1.officebooking.model.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    Optional<PendingRegistration> findByEmailAndStatus(String email, PendingRegistration.Status status);

    @Query("SELECT pr FROM PendingRegistration pr WHERE pr.organization.id = :orgId AND pr.status = 'PENDING'")
    List<PendingRegistration> findPendingByOrganization(@Param("orgId") Long organizationId);
}


