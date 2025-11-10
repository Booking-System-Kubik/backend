package com.t1.officebooking.dto.response;

import lombok.Builder;

@Builder
public record SpaceResponse(
        Long id,
        Long locationId,
        Long spaceTypeId,
        String spaceType,
        Integer capacity,
        FloorResponse floor,
        Boolean bookable,
        BoundsResponse bounds
) {
    @Builder
    public record BoundsResponse(Integer x, Integer y, Integer width, Integer height) { }
    
    @Builder
    public record FloorResponse(Long id, Integer floorNumber, java.util.List<PointResponse> polygon) { }
    @Builder
    public record PointResponse(Integer x, Integer y) { }
}


