package com.t1.officebooking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TimeIntervalsRequest {
    @NotNull
    private LocalDate date;

    @NotNull
    private Long spaceId;
}
