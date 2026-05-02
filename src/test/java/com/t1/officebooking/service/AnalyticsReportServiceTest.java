package com.t1.officebooking.service;

import com.t1.officebooking.dto.request.ReportRequest;
import com.t1.officebooking.dto.stats.OfficeBookingStats;
import com.t1.officebooking.dto.stats.SpaceBookingStats;
import com.t1.officebooking.exception.AdminAuthorityAbusingException;
import com.t1.officebooking.model.*;
import com.t1.officebooking.model.event.BookingEvent;
import com.t1.officebooking.repository.AnalyticsEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsReportServiceTest {

    @Mock
    private AnalyticsEventRepository analyticsEventRepository;
    @Mock
    private UserService userService;
    @Mock
    private LocationService locationService;

    @InjectMocks
    private AnalyticsReportService analyticsReportService;

    @Test
    void generateBaseReport_projectAdmin_queriesByAdminLocation() {
        ReportRequest req = new ReportRequest();
        req.setStart(LocalDate.of(2026, 1, 1));
        req.setEnd(LocalDate.of(2026, 1, 10));
        req.setTimeZone("UTC");

        UUID adminId = UUID.randomUUID();
        User admin = mock(User.class);
        Location adminLoc = mock(Location.class);
        when(admin.getLocation()).thenReturn(adminLoc);
        when(adminLoc.getId()).thenReturn(44L);
        when(userService.findById(adminId)).thenReturn(admin);
        when(analyticsEventRepository.findBookingsByLocationAndDates(any(), any(), eq(44L)))
                .thenReturn(List.of());

        byte[] csv = analyticsReportService.generateBaseReport(req, true, adminId, null);

        assertThat(new String(csv, StandardCharsets.UTF_8)).startsWith("User Email,Space ID");
        verify(analyticsEventRepository).findBookingsByLocationAndDates(any(), any(), eq(44L));
    }

    @Test
    void generateBaseReport_workspaceAdmin_foreignLocation_throws() {
        ReportRequest req = new ReportRequest();
        req.setStart(LocalDate.of(2026, 1, 1));
        req.setEnd(LocalDate.of(2026, 1, 10));
        req.setTimeZone("UTC");

        UUID adminId = UUID.randomUUID();
        User admin = mock(User.class);
        Organization adminOrg = mock(Organization.class);
        when(admin.getOrganization()).thenReturn(adminOrg);
        when(adminOrg.getId()).thenReturn(1L);
        when(userService.findById(adminId)).thenReturn(admin);

        Location loc = mock(Location.class);
        Organization locOrg = mock(Organization.class);
        when(locOrg.getId()).thenReturn(999L);
        when(loc.getOrganization()).thenReturn(locOrg);
        when(locationService.findById(5L)).thenReturn(loc);

        assertThatThrownBy(() -> analyticsReportService.generateBaseReport(req, false, adminId, 5L))
                .isInstanceOf(AdminAuthorityAbusingException.class);

        verify(analyticsEventRepository, never()).findBookingsByLocationAndDates(any(), any(), any());
    }

    @Test
    void generateSpaceUsageReport_workspaceAdmin_queriesOrganizationWhenNoLocationFilter() {
        ReportRequest req = new ReportRequest();
        req.setStart(LocalDate.of(2026, 2, 1));
        req.setEnd(LocalDate.of(2026, 2, 28));
        req.setTimeZone("Europe/Berlin");

        UUID adminId = UUID.randomUUID();
        User admin = mock(User.class);
        Organization adminOrg = mock(Organization.class);
        when(admin.getOrganization()).thenReturn(adminOrg);
        when(adminOrg.getId()).thenReturn(7L);
        when(userService.findById(adminId)).thenReturn(admin);

        SpaceBookingStats stat = mock(SpaceBookingStats.class);
        when(stat.getLocationCity()).thenReturn("Berlin");
        when(stat.getLocationName()).thenReturn("HQ");
        when(stat.getSpaceId()).thenReturn(3L);
        when(stat.getBookingCount()).thenReturn(12L);

        when(analyticsEventRepository.findSpaceBookingStatsByOrganization(any(), any(), eq(7L)))
                .thenReturn(List.of(stat));

        byte[] csv = analyticsReportService.generateSpaceUsageReport(req, false, adminId, null);

        assertThat(new String(csv, StandardCharsets.UTF_8)).contains("Berlin");
        assertThat(new String(csv, StandardCharsets.UTF_8)).contains("12");
    }

    @Test
    void generateLocationLoadReport_projectAdmin_singleStatRow() {
        ReportRequest req = new ReportRequest();
        req.setStart(LocalDate.of(2026, 3, 1));
        req.setEnd(LocalDate.of(2026, 3, 31));
        req.setTimeZone("UTC");

        UUID adminId = UUID.randomUUID();
        User admin = mock(User.class);
        Location adminLoc = mock(Location.class);
        when(admin.getLocation()).thenReturn(adminLoc);
        when(adminLoc.getId()).thenReturn(2L);
        when(userService.findById(adminId)).thenReturn(admin);

        OfficeBookingStats stats = mock(OfficeBookingStats.class);
        when(stats.getLocationCity()).thenReturn("NSK");
        when(stats.getLocationName()).thenReturn("Office");
        when(stats.getBookingCount()).thenReturn(5L);
        when(analyticsEventRepository.findLocationBookingStats(any(), any(), eq(2L))).thenReturn(stats);

        byte[] csv = analyticsReportService.generateLocationLoadReport(req, true, adminId, null);

        assertThat(new String(csv, StandardCharsets.UTF_8)).contains("NSK");
        assertThat(new String(csv, StandardCharsets.UTF_8)).contains("5");
    }

    @Test
    void processAnalyticsEvent_persistsMappedFields() {
        User user = mock(User.class);
        Space space = mock(Space.class);
        Location location = mock(Location.class);

        BookingEvent event = new BookingEvent(
                this,
                user,
                space,
                location,
                null,
                null,
                "CONFIRMED",
                LocalDateTime.now(ZoneOffset.UTC),
                30,
                null
        );

        AnalyticsEvent saved = AnalyticsEvent.builder().id(1L).build();
        when(analyticsEventRepository.save(any(AnalyticsEvent.class))).thenReturn(saved);

        AnalyticsEvent result = analyticsReportService.processAnalyticsEvent(event);

        assertThat(result.getId()).isEqualTo(1L);
        verify(analyticsEventRepository).save(any(AnalyticsEvent.class));
    }
}
