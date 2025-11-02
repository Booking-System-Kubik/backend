package com.t1.officebooking.controller;

import com.t1.officebooking.authorization.security.CustomUserDetails;
import com.t1.officebooking.dto.request.ReportRequest;
import com.t1.officebooking.model.UserRole;
import com.t1.officebooking.service.AnalyticsReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AnalyticsReportController {

    private final AnalyticsReportService reportService;

    @PostMapping("/analytics/csv")
    public ResponseEntity<byte[]> getBaseReportAsCsv(
            @RequestBody @Valid ReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        byte[] csvBytes = reportService.generateBaseReport(
                        request,
                        isProjectAdmin(userDetails),
                        UUID.fromString(userDetails.getUserId()),
                null);

        return getResponseWithCsvBody(csvBytes);
    }


    @PostMapping("/analytics/location/{locationId}/csv")
    public ResponseEntity<byte[]> getBaseReportByLocationAsCsv(
            @PathVariable Long locationId,
            @RequestBody @Valid ReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        byte[] csvBytes = reportService.generateBaseReport(
                request,
                false,
                UUID.fromString(userDetails.getUserId()),
                locationId);

        return getResponseWithCsvBody(csvBytes);
    }


    @PostMapping("/analytics/space-load/csv")
    public ResponseEntity<byte[]> getSpaceLoadReportAsCsv(
            @RequestBody @Valid ReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        byte[] csvBytes = reportService.generateSpaceUsageReport(
                request,
                isProjectAdmin(userDetails),
                UUID.fromString(userDetails.getUserId()),
                null
        );

        return getResponseWithCsvBody(csvBytes);
    }


    @PostMapping("/analytics/location/{locationId}/space-load/csv")
    public ResponseEntity<byte[]> getSpaceLoadReportByLocationAsCsv(
            @PathVariable Long locationId,
            @RequestBody @Valid ReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        byte[] csvBytes = reportService.generateSpaceUsageReport(
                request,
                false,
                UUID.fromString(userDetails.getUserId()),
                locationId
        );

        return getResponseWithCsvBody(csvBytes);
    }


    @PostMapping("/analytics/location-load/csv")
    public ResponseEntity<byte[]> getLocationLoadReportAsCsv(
            @RequestBody @Valid ReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        byte[] csvBytes = reportService.generateLocationLoadReport(
                request,
                isProjectAdmin(userDetails),
                UUID.fromString(userDetails.getUserId()),
                null
        );

        return getResponseWithCsvBody(csvBytes);
    }


    @PostMapping("/analytics/location/{locationId}/location-load/csv")
    public ResponseEntity<byte[]> getLocationLoadReportByLocationAsCsv(
            @PathVariable Long locationId,
            @RequestBody @Valid ReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        byte[] csvBytes = reportService.generateLocationLoadReport(
                request,
                false,
                UUID.fromString(userDetails.getUserId()),
                locationId
        );

        return getResponseWithCsvBody(csvBytes);
    }


    private boolean isProjectAdmin(CustomUserDetails userDetails) {
        return !userDetails.getRoles().contains(UserRole.ROLE_ADMIN_WORKSPACE);
    }

    private ResponseEntity<byte[]> getResponseWithCsvBody(byte[] csvBytes) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=analytics_report.csv")
                .body(csvBytes);
    }
}
