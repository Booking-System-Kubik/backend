package com.t1.officebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CreatingLocationRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String address;

    @NotNull
    private Boolean isActive;

    @NotNull
    private LocalTime workDayStart;

    @NotNull
    private LocalTime workDayEnd;

    @NotBlank
    private String timeZone;

    @NotNull
    private Long organizationId;
}
