package com.example.library_system.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzScheduleSynchronizer implements ApplicationRunner {

    private final Scheduler scheduler;
    private final List<JobDetail> jobDetails;
    private final List<Trigger> triggers;

    @Override
    public void run(ApplicationArguments args) throws SchedulerException {
        synchronize();
    }

    void synchronize() throws SchedulerException {
        for (JobDetail jobDetail : jobDetails) {
            scheduler.addJob(jobDetail, true);
        }
        for (Trigger configured : triggers) {
            Trigger stored = scheduler.getTrigger(configured.getKey());
            if (stored == null) {
                scheduler.scheduleJob(configured);
                log.info("Тригер {} додано до розкладу", configured.getKey());
            } else if (!sameSchedule(stored, configured)) {
                scheduler.rescheduleJob(configured.getKey(), configured);
                log.info("Розклад тригера {} змінено в конфігурації — тригер оновлено", configured.getKey());
            } else {
                log.info("Тригер {} без змін — збережений стан і пропущені запуски не чіпаємо", configured.getKey());
            }
        }
    }

    static boolean sameSchedule(Trigger stored, Trigger configured) {
        if (stored instanceof CronTrigger storedCron && configured instanceof CronTrigger configuredCron) {
            return Objects.equals(storedCron.getCronExpression(), configuredCron.getCronExpression())
                    && Objects.equals(storedCron.getTimeZone().getID(), configuredCron.getTimeZone().getID())
                    && storedCron.getMisfireInstruction() == configuredCron.getMisfireInstruction()
                    && Objects.equals(storedCron.getJobKey(), configuredCron.getJobKey())
                    && Objects.equals(storedCron.getJobDataMap(), configuredCron.getJobDataMap());
        }
        return false;
    }
}