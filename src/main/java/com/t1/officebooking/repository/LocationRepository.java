package com.t1.officebooking.repository;

import com.t1.officebooking.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    boolean existsByCityAndAddressAndOrganization_Id(String city, String address, Long organizationId);

    java.util.List<Location> findByOrganization_Id(Long organizationId);
}
