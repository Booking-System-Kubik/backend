package com.t1.officebooking.service;

import com.t1.officebooking.authorization.dto.UserDataResponse;
import com.t1.officebooking.dto.request.ChangingRoleRequest;
import com.t1.officebooking.exception.AdminAuthorityAbusingException;
import com.t1.officebooking.exception.RoleAssignmentViolationException;
import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.User;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void assignRole(ChangingRoleRequest request,
                           Boolean isProjectAdmin, UUID adminId) {
        if (isProjectAdmin && request.getRole().equals(UserRole.ROLE_ADMIN_WORKSPACE))
            throw new RoleAssignmentViolationException
                    ("Project Admin cant give the role of Workspace Admin to anyone");


        User user = findByEmail(request.getEmail());

        if (isProjectAdmin && user.getRoles().contains(UserRole.ROLE_ADMIN_WORKSPACE))
            throw new RoleAssignmentViolationException
                    ("Project Admin cant change WorkSpace Admin's roles");

        if (isProjectAdmin && isUserNotFromAdminLocation(user.getLocation(), adminId))
            throw new AdminAuthorityAbusingException("Doing Actions on employees " +
                    "of another department is forbidden");

        // Workspace Admin: check organization
        if (!isProjectAdmin && isUserNotFromAdminOrganization(user, adminId))
            throw new AdminAuthorityAbusingException("Doing Actions on employees " +
                    "of another organization is forbidden");

        if (request.getRole().equals(UserRole.ROLE_ADMIN_WORKSPACE)) {
            user.addRole(UserRole.ROLE_ADMIN_PROJECT);
        }
        user.addRole(request.getRole());
        userRepository.save(user);
    }

    @Transactional
    public void revokeRoleFromUser(ChangingRoleRequest request,
                                   Boolean isProjectAdmin, UUID adminId) {
        User user = findByEmail(request.getEmail());

        if (isProjectAdmin && (request.getRole().equals(UserRole.ROLE_ADMIN_WORKSPACE)
                || user.getRoles().contains(UserRole.ROLE_ADMIN_WORKSPACE)))
            throw new RoleAssignmentViolationException
                    ("Project Admin cant revoke the role from Workspace Admin");

        if (isProjectAdmin && isUserNotFromAdminLocation(user.getLocation(), adminId))
            throw new AdminAuthorityAbusingException("Doing Actions on employees " +
                "of another department is forbidden");

        // Workspace Admin: check organization
        if (!isProjectAdmin && isUserNotFromAdminOrganization(user, adminId))
            throw new AdminAuthorityAbusingException("Doing Actions on employees " +
                    "of another organization is forbidden");

        if (request.getRole().equals(UserRole.ROLE_ADMIN_PROJECT))
            user.removeRole(UserRole.ROLE_ADMIN_WORKSPACE);

        user.removeRole(request.getRole());
        userRepository.save(user);
    }

    @Transactional
    public boolean isUserNotFromAdminLocation(Location userLocation, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Cant find your admin account"));

        if (userLocation == null)
            return true;
        return !userLocation.equals(admin.getLocation());
    }

    @Transactional
    public boolean isUserNotFromAdminOrganization(User user, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Cant find your admin account"));

        if (user.getOrganization() == null || admin.getOrganization() == null)
            return true;
        return !user.getOrganization().getId().equals(admin.getOrganization().getId());
    }

    @Transactional
    public UserDataResponse getUserData(UUID userId) {
        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new EntityNotFoundException("User must exist"));

        return new UserDataResponse(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException
                        ("User with provided email does not exist"));
    }

    @Transactional
    public User findByEmailWithDetails(String email) {
        return userRepository.findByEmailWithDetails(email)
                .orElseThrow(() -> new EntityNotFoundException
                        ("User with provided email does not exist"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User must exist"));
    }

    @Transactional
    public List<UserDataResponse> getAllUsersByAdmin(UUID adminId,
                                         Boolean isProjectAdmin) {
        User admin = findById(adminId);

        List<User> users = isProjectAdmin
                ? userRepository.findByLocation(admin.getLocation().getId())
                : userRepository.findByOrganization(admin.getOrganization().getId());

        return users.stream().map(UserDataResponse::new).toList();
    }

    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<UserDataResponse> findUsersByLocation(Long locationId) {
        return userRepository.findByLocation(locationId)
                .stream().map(UserDataResponse::new).toList();
    }

    @Transactional
    public void removeUserFromOrganization(String email, Boolean isProjectAdmin, UUID adminId) {
        User user = findByEmailWithDetails(email);

        // Project Admin cannot remove Workspace Admin
        if (isProjectAdmin && user.getRoles().contains(UserRole.ROLE_ADMIN_WORKSPACE)) {
            throw new RoleAssignmentViolationException
                    ("Project Admin cannot remove Workspace Admin from organization");
        }

        // Project Admin: check location
        if (isProjectAdmin && isUserNotFromAdminLocation(user.getLocation(), adminId)) {
            throw new AdminAuthorityAbusingException("Doing Actions on employees " +
                    "of another location is forbidden");
        }

        // Workspace Admin: check organization
        if (!isProjectAdmin && isUserNotFromAdminOrganization(user, adminId)) {
            throw new AdminAuthorityAbusingException("Doing Actions on employees " +
                    "of another organization is forbidden");
        }

        // Delete user completely from database
        userRepository.delete(user);
    }
}
