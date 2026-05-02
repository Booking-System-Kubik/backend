package com.t1.officebooking.service.mapper;

import com.t1.officebooking.dto.response.BookingResponse;
import com.t1.officebooking.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperFunctionTest {

    private final BookingMapperFunction mapper = new BookingMapperFunction();

    @Test
    void apply_mapsFieldsFromBookingGraph() {
        Organization org = new Organization("O");
        org.setId(1L);

        Location loc = new Location("HQ", "City", "Street", true,
                java.time.LocalTime.of(9, 0),
                java.time.LocalTime.of(18, 0),
                "UTC");
        loc.setId(10L);
        loc.setOrganization(org);

        SpaceType st = new SpaceType("MEETING_ROOM", java.util.List.of("PT1H"), loc);
        st.setId(3L);

        Floor floor = new Floor(loc, 1, java.util.List.of());
        floor.setId(5L);

        Space space = new Space(loc, st, 6, floor);
        space.setId(100L);

        User user = new User();
        user.setEmail("booker@corp.com");

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        Booking booking = Booking.builder()
                .id(77L)
                .user(user)
                .space(space)
                .start(now)
                .end(now.plusHours(1))
                .bookingType("REGULAR")
                .status("CONFIRMED")
                .createdAt(now)
                .updatedAt(now)
                .build();

        BookingResponse dto = mapper.apply(booking);

        assertThat(dto.id()).isEqualTo(77L);
        assertThat(dto.userEmail()).isEqualTo("booker@corp.com");
        assertThat(dto.spaceId()).isEqualTo(100L);
        assertThat(dto.locationId()).isEqualTo(10L);
        assertThat(dto.spaceName()).isEqualTo("MEETING_ROOM");
        assertThat(dto.locationName()).isEqualTo("HQ");
        assertThat(dto.bookingType()).isEqualTo("REGULAR");
        assertThat(dto.status()).isEqualTo("CONFIRMED");
    }

    @Test
    void apply_null_returnsNull() {
        assertThat(mapper.apply(null)).isNull();
    }
}
