package com.t1.officebooking.service.mapper;

import com.t1.officebooking.dto.response.BookingResponse;
import com.t1.officebooking.model.Booking;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class BookingMapperFunction implements Function<Booking, BookingResponse> {

    @Override
    public BookingResponse apply(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .userEmail(booking.getUser().getEmail())
                .spaceId(booking.getSpace().getId())
                .locationId(booking.getSpace().getLocation().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .bookingType(booking.getBookingType())
                .spaceName(booking.getSpace().getSpaceType().getType())
                .locationName(booking.getSpace().getLocation().getName())
                .build();
    }
}
