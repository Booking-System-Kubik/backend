package com.t1.officebooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class TimeSlotResponse {
    private String offset;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
    private List<String> availableDurations;
}
