package com.t1.officebooking.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BookingResponse(
        Long id,
        String userEmail,
        String locationName,
        Long locationId,
        String spaceName,
        Long spaceId,
        LocalDateTime start,
        LocalDateTime end,
        String bookingType,
        String status
) { }

