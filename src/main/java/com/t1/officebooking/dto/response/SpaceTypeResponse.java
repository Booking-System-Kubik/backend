package com.t1.officebooking.dto.response;

import lombok.Builder;

@Builder
public record SpaceTypeResponse(
        Long id,
        String type
) {
}
