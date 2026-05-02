package com.t1.officebooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.t1.officebooking.authorization.security.CustomUserDetails;
import com.t1.officebooking.dto.request.BookingRequest;
import com.t1.officebooking.dto.request.FilteringSpacesRequest;
import com.t1.officebooking.dto.request.TimeIntervalsRequest;
import com.t1.officebooking.dto.response.BookingResponse;
import com.t1.officebooking.dto.response.SpaceResponse;
import com.t1.officebooking.authorization.service.JwtTokenService;
import com.t1.officebooking.model.Space;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.service.BookingService;
import com.t1.officebooking.service.SpaceService;
import com.t1.officebooking.service.mapper.SpaceMapperFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpaceService spaceService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private SpaceMapperFunction spaceMapper;

    @MockBean
    private JwtTokenService jwtTokenService;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void spaceFilter_returnsOk() throws Exception {
        FilteringSpacesRequest req = new FilteringSpacesRequest();
        req.setLocationId(1L);
        req.setSpaceTypeId(2L);

        Space space = org.mockito.Mockito.mock(Space.class);
        when(space.getId()).thenReturn(10L);
        when(spaceService.getFilteredSpaces(any())).thenReturn(List.of(space));
        when(spaceMapper.apply(space)).thenReturn(SpaceResponse.builder().id(10L).build());

        mockMvc.perform(post("/api/booking/space-filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void timeIntervals_returnsOk() throws Exception {
        TimeIntervalsRequest req = new TimeIntervalsRequest();
        req.setDate(LocalDate.of(2026, 6, 1));
        req.setSpaceId(5L);

        when(bookingService.getDayAvailability(any())).thenReturn(List.of());

        mockMvc.perform(post("/api/booking/time-intervals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void bookSpace_returnsCreated() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserDetails principal = new CustomUserDetails(
                userId.toString(),
                "user@test.com",
                List.of(new SimpleGrantedAuthority(UserRole.ROLE_USER.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        LocalDateTime start = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);
        BookingRequest req = new BookingRequest();
        req.setSpaceId(1L);
        req.setType("REGULAR");
        req.setStart(start);
        req.setEnd(start.plusHours(1));

        when(bookingService.bookSpace(any(), eq(userId), eq(true)))
                .thenReturn(BookingResponse.builder().id(9L).build());

        mockMvc.perform(post("/api/booking/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void activeBookings_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserDetails principal = new CustomUserDetails(
                userId.toString(),
                "user@test.com",
                List.of(new SimpleGrantedAuthority(UserRole.ROLE_USER.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        when(bookingService.getAllActiveUserBookings(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/booking/active-bookings"))
                .andExpect(status().isOk());
    }
}
