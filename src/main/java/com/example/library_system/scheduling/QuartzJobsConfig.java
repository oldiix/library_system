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

    private final SchedulingProperties properties;

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
}