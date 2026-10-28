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
}