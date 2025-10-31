package com.t1.officebooking.service.handler;

import com.t1.officebooking.model.AnalyticsEvent;
import com.t1.officebooking.model.event.BookingEvent;
import com.t1.officebooking.service.AnalyticsReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsEventHandler {
    private final AnalyticsReportService analyticsReportService;

    @EventListener
    public void handleAnalyticsEvent(BookingEvent event) {
        analyticsReportService.processAnalyticsEvent(event);
    }
}