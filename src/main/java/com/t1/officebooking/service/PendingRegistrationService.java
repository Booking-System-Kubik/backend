package com.t1.officebooking.service;

import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.Organization;
import com.t1.officebooking.model.PendingRegistration;
import com.t1.officebooking.model.User;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.repository.PendingRegistrationRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PendingRegistrationService {
    private final PendingRegistrationRepository repository;
    private final UserService userService;
    private final OrganizationService organizationService;
    private final LocationService locationService;

    @Transactional
    public PendingRegistration create(String fullName, String email, String position, String hashedPassword,
                                      Long organizationId, Long locationId) {
        if (userService.existsByEmail(email)) {
            throw new EntityExistsException("Email already exists");
        }
        repository.findByEmailAndStatus(email, PendingRegistration.Status.PENDING)
                .ifPresent(pr -> { throw new EntityExistsException("Pending registration already exists"); });

        Organization org = organizationService.findById(organizationId);
        Location loc = locationId != null ? locationService.findById(locationId) : null;

        PendingRegistration pr = new PendingRegistration();
        pr.setFullName(fullName);
        pr.setEmail(email);
        pr.setPosition(position);
        pr.setHashedPassword(hashedPassword);
        pr.setOrganization(org);
        pr.setLocation(loc);
        pr.setStatus(PendingRegistration.Status.PENDING);
        pr.setCreatedAt(LocalDateTime.now());
        return repository.save(pr);
    }

    public List<PendingRegistration> findPendingByOrganization(Long organizationId) {
        return repository.findPendingByOrganization(organizationId);
    }

    public PendingRegistration findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pending registration not found"));
    }

    @Transactional
    public void approve(Long id, String reviewer) {
        PendingRegistration pr = findById(id);
        if (userService.existsByEmail(pr.getEmail())) {
            throw new EntityExistsException("Email already exists");
        }
        User user = new User(
                pr.getLocation(),
                pr.getFullName(),
                pr.getEmail(),
                Set.of(UserRole.ROLE_USER),
                pr.getPosition(),
                pr.getHashedPassword()
        );
        user.setOrganization(pr.getOrganization());
        userService.saveUser(user);

        pr.setStatus(PendingRegistration.Status.APPROVED);
        pr.setReviewedAt(LocalDateTime.now());
        pr.setReviewedBy(reviewer);
        repository.save(pr);
    }

    @Transactional
    public void reject(Long id, String reviewer) {
        PendingRegistration pr = findById(id);
        pr.setStatus(PendingRegistration.Status.REJECTED);
        pr.setReviewedAt(LocalDateTime.now());
        pr.setReviewedBy(reviewer);
        repository.save(pr);
    }
}


