package com.t1.officebooking.service;

import com.t1.officebooking.dto.request.BookingByAdminRequest;
import com.t1.officebooking.dto.request.BookingRequest;
import com.t1.officebooking.dto.request.TimeIntervalsRequest;
import com.t1.officebooking.dto.response.BookingResponse;
import com.t1.officebooking.dto.response.TimeSlotResponse;
import com.t1.officebooking.exception.IncorrectBookingException;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.model.event.BookingEvent;
import com.t1.officebooking.exception.AdminAuthorityAbusingException;
import com.t1.officebooking.exception.RefactoringForeignBookingsException;
import com.t1.officebooking.exception.SlotAlreadyBookedException;
import com.t1.officebooking.model.Booking;
import com.t1.officebooking.model.Space;
import com.t1.officebooking.model.User;
import com.t1.officebooking.repository.BookingRepository;
import com.t1.officebooking.service.mapper.BookingMapperFunction;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ApplicationEventPublisher eventPublisher;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final SpaceService spaceService;
    private final BookingMapperFunction bookingMapper;

    @Transactional
    public void cancelBookingByAdmin(long bookingId, UUID adminId, Boolean isProjectAdmin) {
        Booking booking = findById(bookingId);

        User admin = userService.findById(adminId);

        if (isProjectAdmin && !booking.getSpace().getLocation().equals(admin.getLocation()))
            throw new AdminAuthorityAbusingException("Doing Actions on Bookings " +
                    "from another office is forbidden");

        // Workspace Admin: check organization
        if (!isProjectAdmin) {
            if (!booking.getSpace().getLocation().getOrganization().getId().equals(admin.getOrganization().getId())) {
                throw new AdminAuthorityAbusingException("Doing Actions on Bookings " +
                        "from another organization is forbidden");
            }
        }

        booking.setStatus("CANCELLED");

        bookingRepository.save(booking);

        publishBookingEvent(booking);
    }

    @Transactional
    public void cancelBooking(long bookingId, UUID userId) {
        Booking booking = findByIdWithDetails(bookingId);

        if (!booking.getUser().getId().equals(userId))
            throw new RefactoringForeignBookingsException("You can only cancel your bookings");

        booking.setStatus("CANCELLED");

        bookingRepository.save(booking);

        publishBookingEvent(booking);
    }

    @Transactional
    public BookingResponse bookSpace(BookingRequest request, UUID userId, Boolean isUser) {

        Space space = spaceService.findSpaceById(request.getSpaceId());

        validateBookingRequest(request, space, isUser, userId);

        Long bookingId =  bookingRepository.createBookingIfAvailable(
                userId,
                space.getId(),
                request.getStart(),
                request.getEnd(),
                request.getType(),
                "CONFIRMED",
                LocalDateTime.now(ZoneOffset.UTC),
                LocalDateTime.now(ZoneOffset.UTC)
        ).orElseThrow(() -> new SlotAlreadyBookedException("This slot has been already booked"));

        Booking booking = findByIdWithDetails(bookingId);

        publishBookingEvent(booking);

        return bookingMapper.apply(booking);
    }

    @Transactional
    public BookingResponse bookSpaceByAdmin(BookingByAdminRequest request,
                                            UUID adminId, Boolean isProjectAdmin) {
        Space space = spaceService.findSpaceById(request.getSpaceId());

        User admin  = userService.findById(adminId);

        User user = userService.findByEmail(request.getUserEmail());

        Boolean userIsNotAdmin = !user.getRoles().contains(UserRole.ROLE_ADMIN_WORKSPACE) &&
                !user.getRoles().contains(UserRole.ROLE_ADMIN_PROJECT);

        if (isProjectAdmin && userService.isUserNotFromAdminLocation(user.getLocation(), adminId))
            throw new AdminAuthorityAbusingException("Project admin cant book space for employee from another location");
        if (isProjectAdmin && !space.getLocation().equals(admin.getLocation()))
            throw new AdminAuthorityAbusingException("Project admin cant book space for employee outside his location");

        // Workspace Admin: check organization
        if (!isProjectAdmin) {
            if (userService.isUserNotFromAdminOrganization(user, adminId))
                throw new AdminAuthorityAbusingException("Workspace admin cant book space for employee from another organization");
            if (!space.getLocation().getOrganization().getId().equals(admin.getOrganization().getId()))
                throw new AdminAuthorityAbusingException("Workspace admin cant book space outside his organization");
        }

        validateBookingRequest(new BookingRequest(request), space, userIsNotAdmin, user.getId());

        Long bookingId =  bookingRepository.createBookingIfAvailable(
                user.getId(),
                space.getId(),
                request.getStart(),
                request.getEnd(),
                request.getType(),
                "CONFIRMED",
                LocalDateTime.now(ZoneOffset.UTC),
                LocalDateTime.now(ZoneOffset.UTC)
        ).orElseThrow(() -> new SlotAlreadyBookedException("This slot has been already booked"));

        Booking booking = findByIdWithDetails(bookingId);

        publishBookingEvent(booking);

        return bookingMapper.apply(booking);
    }


    @Transactional
    public List<BookingResponse> getAllActiveBookingsByAdmin(
            UUID adminId, Boolean isProjectAdmin) {

        User admin = userService.findById(adminId);

        if (isProjectAdmin)
            return bookingRepository.findActiveBookingsByLocation(admin.getLocation().getId(),
                            LocalDateTime.now(ZoneOffset.UTC))
                    .stream().map(bookingMapper).toList();
        return bookingRepository.findActiveBookingsByOrganization(
                admin.getOrganization().getId(),
                LocalDateTime.now(ZoneOffset.UTC))
                .stream().map(bookingMapper).toList();
    }

    public List<BookingResponse> getAllActiveUserBookings(UUID userId) {
        return bookingRepository
                .findActiveBookingsByUserId(userId, LocalDateTime.now(ZoneOffset.UTC))
                .stream().map(bookingMapper).toList();
    }

    public List<BookingResponse> getAllUserBookings(UUID userId) {
        return bookingRepository.findAllBookingsByUserId(userId)
                .stream().map(bookingMapper).toList();
    }

    @Transactional
    public List<BookingResponse> getUserBookingsByAdmin(
            String userEmail, UUID adminId,
            Boolean isProjectAdmin, Boolean onlyActiveBookings) {
        User user = userService.findByEmail(userEmail);
        User admin = userService.findById(adminId);

        if (isProjectAdmin && userService.isUserNotFromAdminLocation(user.getLocation(), adminId))
            throw new AdminAuthorityAbusingException("Doing Actions on employees " +
                    "of another office is forbidden");

        if (isProjectAdmin) {
            if (onlyActiveBookings)
                return bookingRepository
                        .findActiveBookingsByLocationAndUser(
                                user.getLocation().getId(),
                                user.getId(),
                                LocalDateTime.now(ZoneOffset.UTC))
                        .stream().map(bookingMapper).toList();
            return bookingRepository.
                    findBookingsByLocationAndUser(
                        user.getLocation().getId(),
                        user.getId())
                    .stream().map(bookingMapper).toList();

        }

        // Workspace Admin: check organization
        if (userService.isUserNotFromAdminOrganization(user, adminId))
            throw new AdminAuthorityAbusingException("Doing Actions on employees " +
                    "of another organization is forbidden");

        if (onlyActiveBookings)
            return bookingRepository.findActiveBookingsByUserId(
                    user.getId(),
                    LocalDateTime.now(ZoneOffset.UTC))
                    .stream().map(bookingMapper).toList();

        return bookingRepository.findAllBookingsByUserId(user.getId())
                .stream().map(bookingMapper).toList();
    }

    public void publishBookingEvent(Booking booking) {
        eventPublisher.publishEvent(
                new BookingEvent(
                    this,
                        booking.getUser(),
                        booking.getSpace(),
                        booking.getSpace().getLocation(),
                        booking.getUser().getDepartment(),
                        booking.getUser().getLegalEntity(),
                        booking.getStatus(),
                        LocalDateTime.now(ZoneOffset.UTC),
                        (int) Duration.between(booking.getStart(), booking.getEnd()).toMinutes(),
                        null  //TODO  METADATA
                )
        );
    }

    @Transactional
    public List<TimeSlotResponse> getDayAvailability(TimeIntervalsRequest request) {
        Space space = spaceService.findSpaceById(request.getSpaceId());

        List<Duration> allowedDurations = space.getSpaceType().getAllowedDurations()
                .stream().map(Duration::parse).toList();


        String timeZone = space.getLocation().getTimeZone();

        ZoneId zone = ZoneId.of(timeZone);

        ZonedDateTime startZoned = ZonedDateTime.of(
                request.getDate(),
                space.getLocation().getWorkDayStart(),
                zone);
        ZonedDateTime endZoned = ZonedDateTime.of(
                request.getDate(),
                space.getLocation().getWorkDayEnd(),
                zone);

        LocalDateTime dayStart = startZoned.toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        LocalDateTime dayEnd = endZoned.toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();

        List<Booking> bookings = bookingRepository.findActiveBookingsForPeriod(request.getSpaceId(), dayStart, dayEnd);
        bookings.sort(Comparator.comparing(Booking::getStart));

        List<TimeSlotResponse> result = new ArrayList<>();
        LocalDateTime currentTime = dayStart;

        String offset = getCurrentOffset(timeZone).toString();

        for (Booking booking : bookings) {
            if (!booking.getStart().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                if (booking.getStart().isAfter(currentTime)) {
                    result.add(createAvailableSlot(
                            currentTime.isAfter(LocalDateTime.now(ZoneOffset.UTC))
                                    ? currentTime
                                    : LocalDateTime.now(ZoneOffset.UTC).plusMinutes(5)
                                    .truncatedTo(ChronoUnit.MINUTES),
                            booking.getStart(),
                            allowedDurations,
                            dayEnd,
                            space.getSpaceType().getType(),
                            offset
                    ));
                }
            }

            currentTime = booking.getEnd();

            if (!booking.getEnd().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                result.add(createBookedSlot(booking, offset));
            }
        }


        if (currentTime.isBefore(dayEnd)) {

            if (currentTime.isBefore(LocalDateTime.now(ZoneOffset.UTC)))
                currentTime = LocalDateTime.now(ZoneOffset.UTC)
                        .plusMinutes(5).truncatedTo(ChronoUnit.MINUTES);

            if (currentTime.isBefore(dayEnd)) {
                result.add(createAvailableSlot(
                        currentTime,
                        dayEnd,
                        allowedDurations,
                        dayEnd,
                        space.getSpaceType().getType(),
                        offset
                ));
            }
        }

        return result;
    }

    private TimeSlotResponse createAvailableSlot(
            LocalDateTime start,
            LocalDateTime end,
            List<Duration> allowedDurations,
            LocalDateTime workdayEnd,
            String spaceType,
            String offset
    ) {
        List<String> availableDurations = allowedDurations.stream()
                .filter(d -> !start.plus(d).isAfter(end)).map(Duration::toString)
                .collect(Collectors.toList());

        if ("WORKSPACE".equals(spaceType) && end.equals(workdayEnd)) {
            Duration tillEndOfDay = Duration.between(start, workdayEnd);
            if (tillEndOfDay.toMinutes() > 30) {
                availableDurations.add(tillEndOfDay.toString());
            }
        }

        return new TimeSlotResponse(
                offset,
                start,
                end,
                "available",
                availableDurations
        );
    }

    private TimeSlotResponse createBookedSlot(Booking booking, String offset) {
        return new TimeSlotResponse(
                offset,
                booking.getStart(),
                booking.getEnd(),
                "booked",
                null
        );
    }

    private void validateBookingRequest(
            BookingRequest request,
            Space space,
            Boolean isUser,
            UUID userId) {

        if (request.getEnd().isBefore(request.getStart()))
            throw new IllegalArgumentException("Booking's start has to be before end");

        if (request.getStart().isBefore(LocalDateTime.now(ZoneOffset.UTC)))
            throw new IncorrectBookingException("Booking's data has to be actual");

        ZoneId zone = ZoneId.of(space.getLocation().getTimeZone());

        ZonedDateTime bookingStart = request.getStart().atZone(ZoneOffset.UTC)
                .withZoneSameInstant(zone);

        ZonedDateTime bookingEnd = request.getEnd().atZone(ZoneOffset.UTC)
                .withZoneSameInstant(zone);

        if (!bookingStart.toLocalDate().equals(bookingEnd.toLocalDate()))
            throw new IncorrectBookingException("The office is closed at this time");

        if (bookingStart.toLocalTime().isBefore(space.getLocation().getWorkDayStart()) ||
        bookingEnd.toLocalTime().isAfter(space.getLocation().getWorkDayEnd()))
            throw new IncorrectBookingException("The office is closed at this time");


        if ((space.getSpaceType().getType().equals("MEETING_ROOM")
                || space.getSpaceType().getType().equals("WORKSPACE")) && isUser) {

            List<Booking> bookings = bookingRepository.
                    findActiveBookingsByUserId(userId, LocalDateTime.now(ZoneOffset.UTC));

            for (Booking booking : bookings) {
                if (booking.getSpace().getSpaceType().equals(space.getSpaceType())) {
                    if (booking.getStart().isBefore(request.getEnd())
                            && booking.getEnd().isAfter(request.getStart()))
                        throw new IncorrectBookingException("User without admin's authorities " +
                                "cant book two Meeting rooms / Workspaces");
                }
            }
        }
    }

    private ZoneOffset getCurrentOffset(String timeZone) {
        ZoneId zoneId = ZoneId.of(timeZone);
        Instant now = Instant.now();
        return zoneId.getRules().getOffset(now);
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking must exist"));
    }

    public List<BookingResponse> findActiveBookingsByLocation(Long locationId) {
       return bookingRepository
               .findActiveBookingsByLocation(locationId, LocalDateTime.now(ZoneOffset.UTC))
               .stream().map(bookingMapper).toList();
    }

    public Booking findByIdWithDetails(Long id) {
        return bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
    }
}
