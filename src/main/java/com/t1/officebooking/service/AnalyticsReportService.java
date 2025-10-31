package com.t1.officebooking.service;

import com.t1.officebooking.dto.request.ReportRequest;
import com.t1.officebooking.dto.stats.OfficeBookingStats;
import com.t1.officebooking.dto.stats.SpaceBookingStats;
import com.t1.officebooking.model.AnalyticsEvent;
import com.t1.officebooking.model.event.BookingEvent;
import com.t1.officebooking.repository.AnalyticsEventRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsReportService {

    private final AnalyticsEventRepository analyticsEventRepository;
    private final UserService userService;
    private final LocationService locationService;

    @Transactional
    public byte[] generateBaseReport(ReportRequest request,
                                             Boolean isProjectAdmin,
                                             UUID adminId,
                                             Long locationOfRequest) {

        List<LocalDateTime> timeInterval = calculateUtcTime(request);
        LocalDateTime dbStart = timeInterval.get(0);
        LocalDateTime dbEnd = timeInterval.get(1);

        List<AnalyticsEvent> events;

        if (isProjectAdmin) {
            Long locationId = userService.findById(adminId).getLocation().getId();

            events = analyticsEventRepository.
                    findBookingsByLocationAndDates(dbStart, dbEnd, locationId);
        }
        else {
            if (locationOfRequest != null) {
                validateLocation(locationOfRequest);
                events = analyticsEventRepository
                        .findBookingsByLocationAndDates(dbStart, dbEnd, locationOfRequest);
            }
            else {
                events = analyticsEventRepository.findAllBookingsBetweenDates(dbStart, dbEnd);
            }
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {

            writer.println("User Email,Space ID,Event Type,Event Timestamp,Location,Department,LegalEntity,Duration (min)");

            for (AnalyticsEvent event : events) {
                writer.println(String.join(",",
                        event.getUser().getEmail(),
                        event.getSpace().getId().toString(),
                        escapeCsv(event.getEventType()),
                        event.getEventTimeStamp().toString(),
                        escapeCsv(event.getLocation().getName()),
                        escapeCsv(event.getDepartment() != null ? event.getDepartment().getName() : ""),
                        escapeCsv(event.getLegalEntity() != null ? event.getLegalEntity().getName(): ""),
                        event.getDurationMinutes() != null ? event.getDurationMinutes().toString() : ""
                ));
            }

            writer.flush();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    @Transactional
    public byte[] generateSpaceUsageReport(ReportRequest request,
                                           Boolean isProjectAdmin,
                                           UUID adminId,
                                           Long locationOfRequest) {

        List<LocalDateTime> timeInterval = calculateUtcTime(request);
        LocalDateTime dbStart = timeInterval.get(0);
        LocalDateTime dbEnd = timeInterval.get(1);

        List<SpaceBookingStats> stats;

        if (isProjectAdmin) {
            Long locationId = userService.findById(adminId).getLocation().getId();
            stats = analyticsEventRepository.findSpaceBookingStatsByLocation(dbStart, dbEnd, locationId);
        } else {
            if (locationOfRequest != null) {
                validateLocation(locationOfRequest);
                stats = analyticsEventRepository.findSpaceBookingStatsByLocation(dbStart, dbEnd, locationOfRequest);
            } else {
                stats = analyticsEventRepository.findSpaceBookingStats(dbStart, dbEnd);
            }

        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {

            writer.println("Location City,Location Name,Space ID,Booking Count");

            for (SpaceBookingStats stat : stats) {
                writer.println(String.join(",",
                        escapeCsv(stat.getLocationCity()),
                        escapeCsv(stat.getLocationName()),
                        stat.getSpaceId().toString(),
                        stat.getBookingCount().toString()
                ));
            }

            writer.flush();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate space usage report", e);
        }
    }

    @Transactional
    public byte[] generateLocationLoadReport(ReportRequest request,
                                           Boolean isProjectAdmin,
                                           UUID adminId,
                                           Long locationOfRequest) {

        List<LocalDateTime> timeInterval = calculateUtcTime(request);
        LocalDateTime dbStart = timeInterval.get(0);
        LocalDateTime dbEnd = timeInterval.get(1);

        List<OfficeBookingStats> stats;

        if (isProjectAdmin) {
            Long locationId = userService.findById(adminId).getLocation().getId();
            stats = List.of(analyticsEventRepository.findLocationBookingStats(dbStart, dbEnd, locationId));
        } else {
            if (locationOfRequest != null) {
                validateLocation(locationOfRequest);
                stats = List.of(analyticsEventRepository.findLocationBookingStats(dbStart, dbEnd, locationOfRequest));
            } else {
                stats = analyticsEventRepository.findAllLocationsBookingStats(dbStart, dbEnd);
            }
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {

            writer.println("Location City,Location Name,Booking Count");

            for (OfficeBookingStats stat : stats) {
                writer.println(String.join(",",
                        escapeCsv(stat.getLocationCity()),
                        escapeCsv(stat.getLocationName()),
                        stat.getBookingCount().toString()
                ));
            }

            writer.flush();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate space usage report", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    @Transactional
    public AnalyticsEvent processAnalyticsEvent(BookingEvent eventData) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .user(eventData.getUser())
                .space(eventData.getSpace())
                .location(eventData.getLocation())
                .department(eventData.getDepartment())
                .legalEntity(eventData.getLegalEntity())
                .eventType(eventData.getEventType())
                .eventTimeStamp(eventData.getEventTimeStamp())
                .durationMinutes(eventData.getDurationMinutes())
                .metadata(eventData.getMetadata())
                .build();

       return analyticsEventRepository.save(event);
    }

    private List<LocalDateTime> calculateUtcTime(ReportRequest request) {
        ZoneId zone = ZoneId.of(request.getTimeZone());
        ZonedDateTime startZoned = request.getStart().atStartOfDay(zone);
        ZonedDateTime endZoned = request.getEnd().plusDays(1).atStartOfDay(zone).minusNanos(1);

        LocalDateTime dbStart = startZoned.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime dbEnd = endZoned.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        return List.of(dbStart, dbEnd);
    }

    private void validateLocation(Long locationId) {
        if (!locationService.locationExists(locationId))
            throw new EntityNotFoundException("Location must exist in order to get report");
    }

}
