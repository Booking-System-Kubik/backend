package com.t1.officebooking.service;

import com.t1.officebooking.dto.request.BookingRequest;
import com.t1.officebooking.dto.response.BookingResponse;
import com.t1.officebooking.exception.AdminAuthorityAbusingException;
import com.t1.officebooking.exception.IncorrectBookingException;
import com.t1.officebooking.exception.RefactoringForeignBookingsException;
import com.t1.officebooking.exception.SlotAlreadyBookedException;
import com.t1.officebooking.model.*;
import com.t1.officebooking.model.event.BookingEvent;
import com.t1.officebooking.repository.BookingRepository;
import com.t1.officebooking.service.mapper.BookingMapperFunction;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private SpaceService spaceService;
    @Mock
    private BookingMapperFunction bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private Organization organization;
    private Location location;
    private SpaceType spaceType;
    private Space space;
    private User user;

    @BeforeEach
    void setUp() {
        organization = new Organization("Org");
        organization.setId(1L);

        location = new Location("L", "C", "A", true,
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                "UTC");
        location.setId(10L);
        location.setOrganization(organization);

        spaceType = new SpaceType("MEETING_ROOM", List.of("PT30M", "PT1H"), location);
        spaceType.setId(3L);

        Floor floor = new Floor(location, 1, List.of());
        floor.setId(7L);

        space = new Space(location, spaceType, 4, floor);
        space.setId(100L);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("u@test.com");
        user.setRoles(Set.of(UserRole.ROLE_USER));
        user.setOrganization(organization);
        user.setLocation(location);
    }

    @Test
    void cancelBooking_whenNotOwner_throws() {
        UUID owner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        Booking booking = new Booking();
        User bookingUser = new User();
        bookingUser.setId(owner);
        booking.setUser(bookingUser);

        when(bookingRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(5L, attacker))
                .isInstanceOf(RefactoringForeignBookingsException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_whenOwner_publishesEvent() {
        UUID id = user.getId();
        Booking booking = Booking.builder()
                .id(5L)
                .user(user)
                .space(space)
                .start(LocalDateTime.now(ZoneOffset.UTC).plusHours(1))
                .end(LocalDateTime.now(ZoneOffset.UTC).plusHours(2))
                .bookingType("REGULAR")
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(bookingRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(5L, id);

        verify(bookingRepository).save(argThat(b -> "CANCELLED".equals(b.getStatus())));
        verify(eventPublisher).publishEvent(any(BookingEvent.class));
    }

    @Test
    void bookSpace_whenEndBeforeStart_throwsIllegalArgument() {
        BookingRequest req = new BookingRequest();
        req.setSpaceId(space.getId());
        req.setType("REGULAR");
        req.setStart(LocalDateTime.now(ZoneOffset.UTC).plusHours(2));
        req.setEnd(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

        when(spaceService.findSpaceById(space.getId())).thenReturn(space);

        assertThatThrownBy(() -> bookingService.bookSpace(req, user.getId(), true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bookSpace_whenSlotTaken_throwsSlotAlreadyBookedException() {
        LocalDateTime start = LocalDateTime.now(ZoneOffset.UTC).plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        BookingRequest req = new BookingRequest();
        req.setSpaceId(space.getId());
        req.setType("REGULAR");
        req.setStart(start);
        req.setEnd(end);

        when(spaceService.findSpaceById(space.getId())).thenReturn(space);
        when(bookingRepository.findActiveBookingsByUserId(eq(user.getId()), any())).thenReturn(List.of());
        when(bookingRepository.createBookingIfAvailable(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.bookSpace(req, user.getId(), true))
                .isInstanceOf(SlotAlreadyBookedException.class);
    }

    @Test
    void bookSpace_success_returnsMappedResponse() {
        LocalDateTime start = LocalDateTime.now(ZoneOffset.UTC).plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        BookingRequest req = new BookingRequest();
        req.setSpaceId(space.getId());
        req.setType("REGULAR");
        req.setStart(start);
        req.setEnd(end);

        Booking saved = Booking.builder()
                .id(42L)
                .user(user)
                .space(space)
                .start(start)
                .end(end)
                .bookingType("REGULAR")
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        BookingResponse mapped = BookingResponse.builder().id(42L).build();

        when(spaceService.findSpaceById(space.getId())).thenReturn(space);
        when(bookingRepository.findActiveBookingsByUserId(eq(user.getId()), any())).thenReturn(List.of());
        when(bookingRepository.createBookingIfAvailable(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(42L));
        when(bookingRepository.findByIdWithDetails(42L)).thenReturn(Optional.of(saved));
        when(bookingMapper.apply(saved)).thenReturn(mapped);

        BookingResponse result = bookingService.bookSpace(req, user.getId(), true);

        assertThat(result).isSameAs(mapped);
        verify(eventPublisher).publishEvent(any(BookingEvent.class));
    }

    @Test
    void cancelBookingByAdmin_projectAdmin_otherLocation_throws() {
        Location adminLoc = new Location("A2", "C", "A", true, LocalTime.MIN, LocalTime.MAX, "UTC");
        adminLoc.setId(99L);
        adminLoc.setOrganization(organization);

        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setLocation(adminLoc);

        Booking booking = Booking.builder()
                .id(1L)
                .user(user)
                .space(space)
                .start(LocalDateTime.now(ZoneOffset.UTC))
                .end(LocalDateTime.now(ZoneOffset.UTC))
                .bookingType("R")
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userService.findById(admin.getId())).thenReturn(admin);

        assertThatThrownBy(() -> bookingService.cancelBookingByAdmin(1L, admin.getId(), true))
                .isInstanceOf(AdminAuthorityAbusingException.class);
    }

    @Test
    void getAllActiveBookingsByAdmin_workspaceAdmin_usesOrganizationQuery() {
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setOrganization(organization);
        admin.setLocation(location);

        when(userService.findById(admin.getId())).thenReturn(admin);
        when(bookingRepository.findActiveBookingsByOrganization(eq(1L), any()))
                .thenReturn(List.of());

        bookingService.getAllActiveBookingsByAdmin(admin.getId(), false);

        verify(bookingRepository).findActiveBookingsByOrganization(eq(1L), any());
        verify(bookingRepository, never()).findActiveBookingsByLocation(any(), any());
    }

    @Test
    void findById_whenMissing_throwsEntityNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void validateBookingRequest_indirectly_startInPast_throwsIncorrectBooking() {
        LocalDateTime past = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
        LocalDateTime end = past.plusHours(2);

        BookingRequest req = new BookingRequest();
        req.setSpaceId(space.getId());
        req.setType("DESK");
        req.setStart(past);
        req.setEnd(end);

        when(spaceService.findSpaceById(space.getId())).thenReturn(space);

        assertThatThrownBy(() -> bookingService.bookSpace(req, user.getId(), true))
                .isInstanceOf(IncorrectBookingException.class);
    }
}
