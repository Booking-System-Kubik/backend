package com.t1.officebooking.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FloorSpacesResponse(
        FloorResponse floor,
        List<SpaceResponse> spaces
) {
    @Builder
    public record FloorResponse(Long id, Integer floorNumber, java.util.List<SpaceResponse.PointResponse> polygon) { }
}

