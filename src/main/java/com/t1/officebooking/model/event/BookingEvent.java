package com.t1.officebooking.model.event;

import com.t1.officebooking.model.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Getter
@Setter

public class BookingEvent extends ApplicationEvent {
    private final User user;
    private final Space space;
    private final Location location;
    private final Department department;
    private final LegalEntity legalEntity;
    private final String eventType;
    private final LocalDateTime eventTimeStamp;
    private final Integer durationMinutes;
    private final String metadata;

    public BookingEvent(Object source,
                         User user,
                         Space space,
                         Location location,
                         Department department,
                         LegalEntity legalEntity,
                         String eventType,
                         LocalDateTime eventTimeStamp,
                         Integer durationMinutes,
                         String metadata) {
        super(source);
        this.user = user;
        this.space = space;
        this.location = location;
        this.department = department;
        this.legalEntity = legalEntity;
        this.eventType = eventType;
        this.eventTimeStamp = eventTimeStamp;
        this.durationMinutes = durationMinutes;
        this.metadata = metadata;
    }
}


