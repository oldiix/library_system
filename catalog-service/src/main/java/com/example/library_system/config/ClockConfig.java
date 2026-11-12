package com.example.library_system.config;

import com.example.library_system.scheduling.SchedulingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(SchedulingProperties schedulingProperties) {
        return Clock.system(schedulingProperties.timeZone().toZoneId());
    }
}