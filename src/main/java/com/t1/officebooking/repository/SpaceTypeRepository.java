package com.t1.officebooking.repository;

import com.t1.officebooking.model.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpaceTypeRepository extends JpaRepository<SpaceType, Long> {
    Optional<SpaceType> findByType(String type);
}
