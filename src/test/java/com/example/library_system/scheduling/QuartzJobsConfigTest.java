package com.example.library_system.scheduling;

import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import static org.assertj.core.api.Assertions.assertThat;

class QuartzJobsConfigTest {

    private final QuartzJobsConfig config = new QuartzJobsConfig(new SchedulingProperties(
            "Europe/Kyiv", "0 0 3 * * ?", "0 0 8 * * ?", "0 0 9 ? * MON"));

    @Test
    void catalogNormalizationJobDetail_hasNameGroupDefaultsAndIsDurable() {
        // Act
        JobDetail jobDetail = config.catalogNormalizationJobDetail();

        // Assert
        assertThat(jobDetail.getKey().getName()).isEqualTo("catalog-normalization");
        assertThat(jobDetail.getKey().getGroup()).isEqualTo("catalog-maintenance");
        assertThat(jobDetail.getJobClass()).isEqualTo(CatalogNormalizationJob.class);
        assertThat(jobDetail.isDurable()).isTrue();
        assertThat(jobDetail.isConcurrentExecutionDisallowed()).isTrue();
        assertThat(jobDetail.getJobDataMap().getInt(CatalogNormalizationJob.BATCH_SIZE)).isEqualTo(100);
    }

    @Test
    void catalogNormalizationNightlyTrigger_pointsToJobByKeyWithCronTimeZoneAndMisfire() {
        // Act
        Trigger trigger = config.catalogNormalizationNightlyTrigger();

        // Assert
        assertThat(trigger.getKey().getName()).isEqualTo("catalog-normalization-nightly");
        assertThat(trigger.getKey().getGroup()).isEqualTo("catalog-maintenance");
        assertThat(trigger.getJobKey()).isEqualTo(QuartzJobsConfig.NORMALIZATION_JOB_KEY);

        assertThat(trigger).isInstanceOf(CronTrigger.class);
        CronTrigger cronTrigger = (CronTrigger) trigger;
        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 0 3 * * ?");
        assertThat(cronTrigger.getTimeZone().getID()).isEqualTo("Europe/Kyiv");
        assertThat(cronTrigger.getMisfireInstruction())
                .isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    }
}