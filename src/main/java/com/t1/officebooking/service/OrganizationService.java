package com.t1.officebooking.service;

import com.t1.officebooking.model.Organization;
import com.t1.officebooking.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    public Organization findById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization must exist"));
    }

    @Transactional
    public Organization create(String name) {
        return organizationRepository.save(new Organization(name));
    }

    public java.util.List<Organization> getAll() {
        return organizationRepository.findAll();
    }
}


