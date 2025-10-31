package com.t1.officebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreatingSpaceTypeRequest {
    @NotBlank
    private String type;

    @NotNull
    private List<String> allowedDurations;
}

