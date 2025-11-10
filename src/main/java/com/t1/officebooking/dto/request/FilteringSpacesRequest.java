package com.t1.officebooking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FilteringSpacesRequest {
    @NotNull
    private Long locationId;
    @NotNull
    private Long spaceTypeId;

    private Integer floorNumber;
}
