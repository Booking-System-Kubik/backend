package com.t1.officebooking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatingSpaceRequest {
    @NotNull
    private Long locationId;

    @NotNull
    private Long spaceTypeId;

    @NotNull
    @Min(value = 1)
    private Integer capacity;

    @NotNull
    private Integer floorNumber;

    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
}
