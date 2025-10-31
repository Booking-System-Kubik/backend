package com.t1.officebooking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingByAdminRequest {
    @NotBlank
    @Email
    private String userEmail;

    @NotNull
    private Long spaceId;

    @NotBlank
    private String type;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;
}
