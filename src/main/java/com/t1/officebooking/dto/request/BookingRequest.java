package com.t1.officebooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BookingRequest {
    @NotNull
    private Long spaceId;

    @NotBlank
    private String type;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

    public BookingRequest(BookingByAdminRequest request) {
        this.spaceId = request.getSpaceId();
        this.start = request.getStart();
        this.end = request.getEnd();
        this.type = request.getType();
    }
}
