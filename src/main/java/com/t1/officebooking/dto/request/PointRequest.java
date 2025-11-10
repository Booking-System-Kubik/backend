package com.t1.officebooking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointRequest {
    @NotNull
    private Integer x;
    @NotNull
    private Integer y;
}


