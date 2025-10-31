package com.t1.officebooking.dto.response;

import com.t1.officebooking.model.PendingRegistration;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PendingRegistrationResponse(
        Long id,
        String email,
        String fullName,
        String position,
        Long organizationId,
        Long locationId,
        String status,
        LocalDateTime createdAt
) {
    public static PendingRegistrationResponse from(PendingRegistration pr) {
        return PendingRegistrationResponse.builder()
                .id(pr.getId())
                .email(pr.getEmail())
                .fullName(pr.getFullName())
                .position(pr.getPosition())
                .organizationId(pr.getOrganization() != null ? pr.getOrganization().getId() : null)
                .locationId(pr.getLocation() != null ? pr.getLocation().getId() : null)
                .status(pr.getStatus().name())
                .createdAt(pr.getCreatedAt())
                .build();
    }
}


