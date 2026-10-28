package com.example.library_system.scheduling;

import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.CronScheduleBuilder;
import org.quartz.TriggerBuilder;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuartzJobsConfigTest {

    private final QuartzJobsConfig config = configWithTimeZone("Europe/Kyiv");


    @Test
    void catalogNormalizationJobDetail_hasNameGroupDefaultsAndIsDurable() {

        JobDetail jobDetail = config.catalogNormalizationJobDetail();

        assertThat(jobDetail.getKey().getName()).isEqualTo("catalog-normalization");
        assertThat(jobDetail.getKey().getGroup()).isEqualTo("catalog-maintenance");
        assertThat(jobDetail.getJobClass()).isEqualTo(CatalogNormalizationJob.class);
        assertThat(jobDetail.isDurable()).isTrue();
        assertThat(jobDetail.isConcurrentExecutionDisallowed()).isTrue();
        assertThat(jobDetail.getJobDataMap().getInt(CatalogNormalizationJob.BATCH_SIZE)).isEqualTo(100);
    }

    @Test
    void catalogNormalizationNightlyTrigger_pointsToJobByKeyWithCronTimeZoneAndMisfire() {

        Trigger trigger = config.catalogNormalizationNightlyTrigger();

        assertThat(trigger.getKey().getName()).isEqualTo("catalog-normalization-nightly");
        assertThat(trigger.getKey().getGroup()).isEqualTo("catalog-maintenance");
        assertThat(trigger.getJobKey()).isEqualTo(QuartzJobsConfig.NORMALIZATION_JOB_KEY);

        CronTrigger cronTrigger = (CronTrigger) trigger;
        assertThat(cronTrigger.getCronExpression()).isEqualTo("0 0 3 * * ?");
        assertThat(cronTrigger.getTimeZone().getID()).isEqualTo("Europe/Kyiv");
        assertThat(cronTrigger.getMisfireInstruction()).isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    }


    @Test
    void stockReportJobDetail_hasNameGroupAndDefaultsForAllParameters() {

        JobDetail jobDetail = config.stockReportJobDetail();

        assertThat(jobDetail.getKey().getName()).isEqualTo("stock-report");
        assertThat(jobDetail.getKey().getGroup()).isEqualTo("reports");
        assertThat(jobDetail.getJobClass()).isEqualTo(StockReportJob.class);
        assertThat(jobDetail.isConcurrentExecutionDisallowed()).isTrue();

        JobDataMap defaults = jobDetail.getJobDataMap();
        assertThat(defaults.getString(StockReportJob.REPORT_NAME)).isEqualTo("daily");
        assertThat(defaults.getInt(StockReportJob.THRESHOLD)).isEqualTo(1);
        assertThat(defaults.getInt(StockReportJob.MAX_TITLES)).isEqualTo(20);
    }

    @Test
    void stockReportTriggers_bothPointToSameJobByKeyAndHaveOwnNamesInReportsGroup() {

        Trigger daily = config.stockReportDailyTrigger();
        Trigger weekly = config.stockReportWeeklyTrigger();


        assertThat(daily.getJobKey()).isEqualTo(QuartzJobsConfig.STOCK_REPORT_JOB_KEY);
        assertThat(weekly.getJobKey()).isEqualTo(QuartzJobsConfig.STOCK_REPORT_JOB_KEY);

        assertThat(daily.getKey().getName()).isEqualTo("stock-report-daily");
        assertThat(weekly.getKey().getName()).isEqualTo("stock-report-weekly");
        assertThat(daily.getKey().getGroup()).isEqualTo("reports");
        assertThat(weekly.getKey().getGroup()).isEqualTo("reports");
    }


    @Test
    void everyTrigger_hasItsOwnExplicitMisfireInstruction() {

        int nightly = config.catalogNormalizationNightlyTrigger().getMisfireInstruction();
        int daily = config.stockReportDailyTrigger().getMisfireInstruction();
        int weekly = config.stockReportWeeklyTrigger().getMisfireInstruction();

        assertThat(nightly).isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        assertThat(daily).isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW);
        assertThat(weekly).isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    }

    @Test
    void cronTriggers_useExplicitTimeZoneInsteadOfServerTimeZone() {

        List<CronTrigger> triggers = List.of(
                (CronTrigger) config.catalogNormalizationNightlyTrigger(),
                (CronTrigger) config.stockReportDailyTrigger(),
                (CronTrigger) config.stockReportWeeklyTrigger());

        assertThat(triggers)
                .extracting(trigger -> trigger.getTimeZone().getID())
                .containsOnly("Europe/Kyiv");
    }

    @Test
    void cronTrigger_unknownTimeZoneId_silentlyFallsBackToGmt() {

        QuartzJobsConfig misconfigured = configWithTimeZone("Europe/Atlantis");

        CronTrigger trigger = (CronTrigger) misconfigured.stockReportDailyTrigger();

        assertThat(trigger.getTimeZone().getID()).isEqualTo("GMT");
    }

    @Test
    void mergedJobData_dailyTriggerWithoutOverrides_usesJobDefaults() {

        JobDataMap merged = mergeLikeQuartz(config.stockReportJobDetail(), config.stockReportDailyTrigger());

        assertThat(merged.getString(StockReportJob.REPORT_NAME)).isEqualTo("daily");
        assertThat(merged.getInt(StockReportJob.THRESHOLD)).isEqualTo(1);
        assertThat(merged.getInt(StockReportJob.MAX_TITLES)).isEqualTo(20);
    }

    @Test
    void mergedJobData_weeklyTrigger_overridesItsKeysAndKeepsOtherDefaults() {

        JobDataMap merged = mergeLikeQuartz(config.stockReportJobDetail(), config.stockReportWeeklyTrigger());

        assertThat(merged.getString(StockReportJob.REPORT_NAME)).isEqualTo("weekly");
        assertThat(merged.getInt(StockReportJob.THRESHOLD)).isEqualTo(3);

        assertThat(merged.getInt(StockReportJob.MAX_TITLES)).isEqualTo(20);
    }

    @Test
    void mergedJobData_triggerKeyWithTypo_isIgnoredAndDocumentedDefaultIsUsed() {

        Trigger triggerWithTypo = TriggerBuilder.newTrigger()
                .withIdentity("stock-report-typo", QuartzJobsConfig.REPORTS_GROUP)
                .forJob(QuartzJobsConfig.STOCK_REPORT_JOB_KEY)
                .usingJobData("treshold", 5)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 * * ?"))
                .build();

        JobDataMap merged = mergeLikeQuartz(config.stockReportJobDetail(), triggerWithTypo);

        assertThat(merged.getInt(StockReportJob.THRESHOLD)).isEqualTo(1);
        assertThat(merged.getInt("treshold")).isEqualTo(5);
    }


    private static QuartzJobsConfig configWithTimeZone(String timeZoneId) {
        return new QuartzJobsConfig(new SchedulingProperties(
                timeZoneId, "0 0 3 * * ?", "0 0 8 * * ?", "0 0 9 ? * MON"));
    }

    private static JobDataMap mergeLikeQuartz(JobDetail jobDetail, Trigger trigger) {
        Date now = new Date();
        TriggerFiredBundle firedBundle = new TriggerFiredBundle(
                jobDetail, (OperableTrigger) trigger, null, false, now, now, null, null);
        return new JobExecutionContextImpl(null, firedBundle, null).getMergedJobDataMap();
    }
}