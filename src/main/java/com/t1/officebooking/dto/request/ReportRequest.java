package com.t1.officebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportRequest {
    @NotNull
    private LocalDate start;

    @NotNull
    private LocalDate end;

    @NotBlank
    private String timeZone;
}
