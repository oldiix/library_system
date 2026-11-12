package com.example.library_system.scheduling;

import com.example.library_system.entity.StockReport;
import com.example.library_system.service.StockReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.TriggerBuilder;
import org.quartz.JobExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReportJobTest {

    @Mock
    private StockReportService stockReportService;

    @Mock
    private JobExecutionContext context;

    @Test
    void execute_weeklyMergedParameters_callsServiceOnceWithThem() throws Exception {

        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap(Map.of(
                StockReportJob.REPORT_NAME, "weekly",
                StockReportJob.THRESHOLD, 3,
                StockReportJob.MAX_TITLES, 20)));
        when(context.getJobDetail()).thenReturn(JobBuilder.newJob(StockReportJob.class)
                .withIdentity(QuartzJobsConfig.STOCK_REPORT_JOB_KEY).build());
        when(context.getTrigger()).thenReturn(TriggerBuilder.newTrigger()
                .withIdentity(QuartzJobsConfig.STOCK_REPORT_WEEKLY_TRIGGER_KEY).build());

        StockReport report = new StockReport();
        report.setReportName("weekly");
        report.setReportDate(LocalDate.of(2026, 10, 26));
        report.setThreshold(3);
        report.setBooksCount(1);
        when(stockReportService.generateReport("weekly", 3, 20)).thenReturn(report);

        StockReportJob job = new StockReportJob(stockReportService);

        job.execute(context);

        verify(stockReportService).generateReport("weekly", 3, 20);
        verifyNoMoreInteractions(stockReportService);
    }

    @Test
    void execute_serviceFails_errorRecordedWithoutImmediateRefire() {

        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap(Map.of(
                StockReportJob.REPORT_NAME, "daily",
                StockReportJob.THRESHOLD, 1,
                StockReportJob.MAX_TITLES, 20)));
        when(context.getJobDetail()).thenReturn(JobBuilder.newJob(StockReportJob.class)
                .withIdentity(QuartzJobsConfig.STOCK_REPORT_JOB_KEY).build());
        when(context.getTrigger()).thenReturn(TriggerBuilder.newTrigger()
                .withIdentity(QuartzJobsConfig.STOCK_REPORT_DAILY_TRIGGER_KEY).build());
        when(stockReportService.generateReport("daily", 1, 20))
                .thenThrow(new IllegalStateException("Помилка формування звіту"));
        StockReportJob job = new StockReportJob(stockReportService);

        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> job.execute(context));

        assertThat(exception.refireImmediately()).isFalse();
        assertThat(exception.unscheduleAllTriggers()).isFalse();
        verify(stockReportService, times(1)).generateReport("daily", 1, 20);
    }
}