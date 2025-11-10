package com.t1.officebooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreatingFloorSpacesRequest {
    @NotNull
    private Long locationId;

    @NotNull
    private Integer floorNumber;

    @NotNull
    @Min(value = 1)
    private Integer width;

    @NotNull
    @Min(value = 1)
    private Integer height;

    @NotEmpty(message = "Spaces list cannot be empty")
    @Valid
    private List<CreatingSpaceRequest> spaces;
}

