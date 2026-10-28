package com.example.library_system.scheduling;

import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QuartzJobsConfig {

    // Впорядкування каталогу

    public static final String MAINTENANCE_GROUP = "catalog-maintenance";

    public static final JobKey NORMALIZATION_JOB_KEY =
            JobKey.jobKey("catalog-normalization", MAINTENANCE_GROUP);

    public static final TriggerKey NORMALIZATION_NIGHTLY_TRIGGER_KEY =
            TriggerKey.triggerKey("catalog-normalization-nightly", MAINTENANCE_GROUP);

    static final int DEFAULT_NORMALIZATION_BATCH_SIZE = 100;

    // Звіт про запаси

    public static final String REPORTS_GROUP = "reports";

    public static final JobKey STOCK_REPORT_JOB_KEY =
            JobKey.jobKey("stock-report", REPORTS_GROUP);

    public static final TriggerKey STOCK_REPORT_DAILY_TRIGGER_KEY =
            TriggerKey.triggerKey("stock-report-daily", REPORTS_GROUP);

    public static final TriggerKey STOCK_REPORT_WEEKLY_TRIGGER_KEY =
            TriggerKey.triggerKey("stock-report-weekly", REPORTS_GROUP);

    static final String DEFAULT_REPORT_NAME = "daily";
    static final int DEFAULT_THRESHOLD = 1;
    static final int DEFAULT_MAX_TITLES = 20;

    static final String WEEKLY_REPORT_NAME = "weekly";
    static final int WEEKLY_THRESHOLD = 3;

    private final SchedulingProperties properties;

    // Впорядкування каталогу

    @Bean
    public JobDetail catalogNormalizationJobDetail() {
        return JobBuilder.newJob(CatalogNormalizationJob.class)
                .withIdentity(NORMALIZATION_JOB_KEY)
                .withDescription("Впорядкування каталогу: зайві пробіли в назвах та іменах, порожні ISBN → null")
                .usingJobData(CatalogNormalizationJob.BATCH_SIZE, DEFAULT_NORMALIZATION_BATCH_SIZE)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger catalogNormalizationNightlyTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(NORMALIZATION_NIGHTLY_TRIGGER_KEY)
                .forJob(NORMALIZATION_JOB_KEY)
                .withDescription("Щоночі о 03:00 за Києвом")
                .withSchedule(CronScheduleBuilder.cronSchedule(properties.catalogNormalizationCron())
                        .inTimeZone(properties.timeZone())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    // Звіт про запаси

    @Bean
    public JobDetail stockReportJobDetail() {
        return JobBuilder.newJob(StockReportJob.class)
                .withIdentity(STOCK_REPORT_JOB_KEY)
                .withDescription("Звіт про книги з малим запасом")
                .usingJobData(StockReportJob.REPORT_NAME, DEFAULT_REPORT_NAME)
                .usingJobData(StockReportJob.THRESHOLD, DEFAULT_THRESHOLD)
                .usingJobData(StockReportJob.MAX_TITLES, DEFAULT_MAX_TITLES)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger stockReportDailyTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(STOCK_REPORT_DAILY_TRIGGER_KEY)
                .forJob(STOCK_REPORT_JOB_KEY)
                .withDescription("Щодня о 08:00 за Києвом, параметри за замовчуванням")
                .withSchedule(CronScheduleBuilder.cronSchedule(properties.dailyStockReportCron())
                        .inTimeZone(properties.timeZone())
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    @Bean
    public Trigger stockReportWeeklyTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(STOCK_REPORT_WEEKLY_TRIGGER_KEY)
                .forJob(STOCK_REPORT_JOB_KEY)
                .withDescription("Щопонеділка о 09:00 за Києвом, розширений поріг")
                .usingJobData(StockReportJob.REPORT_NAME, WEEKLY_REPORT_NAME)
                .usingJobData(StockReportJob.THRESHOLD, WEEKLY_THRESHOLD)
                .withSchedule(CronScheduleBuilder.cronSchedule(properties.weeklyStockReportCron())
                        .inTimeZone(properties.timeZone())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }
}