package com.t1.officebooking.dto.response;

import lombok.Builder;

@Builder
public record SpaceResponse(
        Long id,
        Long locationId,
        Long spaceTypeId,
        String spaceType,
        Integer capacity,
        Integer floor,
        Boolean bookable,
        BoundsResponse bounds
) {
    @Builder
    public record BoundsResponse(Integer x, Integer y, Integer width, Integer height) { }
}


