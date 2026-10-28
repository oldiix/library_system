package com.example.library_system.scheduling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuartzScheduleSynchronizerTest {

    @Mock
    private Scheduler scheduler;

    private final QuartzJobsConfig currentConfig = configWithDailyCron("0 0 8 * * ?");

    @Test
    void synchronize_storedTriggerUnchanged_keepsItsStateWithoutRescheduling() throws Exception {
        Trigger configured = currentConfig.stockReportDailyTrigger();
        when(scheduler.getTrigger(configured.getKey())).thenReturn(currentConfig.stockReportDailyTrigger());
        QuartzScheduleSynchronizer synchronizer = new QuartzScheduleSynchronizer(
                scheduler, List.of(), List.of(configured));

        synchronizer.synchronize();

        verify(scheduler, never()).rescheduleJob(any(), any());
        verify(scheduler, never()).scheduleJob(any(Trigger.class));
    }

    @Test
    void synchronize_cronChangedInConfiguration_reschedulesTrigger() throws Exception {
        Trigger stored = configWithDailyCron("0 0 7 * * ?").stockReportDailyTrigger();
        Trigger configured = currentConfig.stockReportDailyTrigger();
        when(scheduler.getTrigger(configured.getKey())).thenReturn(stored);
        QuartzScheduleSynchronizer synchronizer = new QuartzScheduleSynchronizer(
                scheduler, List.of(), List.of(configured));

        synchronizer.synchronize();

        verify(scheduler).rescheduleJob(configured.getKey(), configured);
    }

    @Test
    void synchronize_jobDetails_updatedWithoutTouchingTheirTriggers() throws Exception {

        JobDetail jobDetail = currentConfig.stockReportJobDetail();
        QuartzScheduleSynchronizer synchronizer = new QuartzScheduleSynchronizer(
                scheduler, List.of(jobDetail), List.of());

        synchronizer.synchronize();

        verify(scheduler).addJob(jobDetail, true);
        verify(scheduler, never()).rescheduleJob(any(), any());
    }

    private static QuartzJobsConfig configWithDailyCron(String dailyCron) {
        return new QuartzJobsConfig(new SchedulingProperties(
                "Europe/Kyiv", "0 0 3 * * ?", dailyCron, "0 0 9 ? * MON"));
    }
}