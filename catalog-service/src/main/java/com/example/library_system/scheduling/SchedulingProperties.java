package com.example.library_system.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.TimeZone;

@ConfigurationProperties(prefix = "library.scheduling")
public record SchedulingProperties(String timeZoneId,
                                   String catalogNormalizationCron,
                                   String dailyStockReportCron,
                                   String weeklyStockReportCron) {

    public TimeZone timeZone() {
        return TimeZone.getTimeZone(timeZoneId);
    }
}