package com.example.library_system.scheduling;

import com.example.library_system.entity.StockReport;
import com.example.library_system.service.StockReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class StockReportJob implements Job {

    public static final String REPORT_NAME = "reportName";

    public static final String THRESHOLD = "threshold";

    public static final String MAX_TITLES = "maxTitles";

    private final StockReportService stockReportService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        String reportName = JobParameters.requiredString(data, REPORT_NAME);
        int threshold = JobParameters.requiredInt(data, THRESHOLD);
        int maxTitles = JobParameters.requiredInt(data, MAX_TITLES);

        StockReport report = stockReportService.generateReport(reportName, threshold, maxTitles);

        log.info("Задача {} (тригер {}): звіт «{}» за {}, поріг {}, книг — {}",
                context.getJobDetail().getKey(), context.getTrigger().getKey(),
                report.getReportName(), report.getReportDate(), report.getThreshold(), report.getBooksCount());
    }
}