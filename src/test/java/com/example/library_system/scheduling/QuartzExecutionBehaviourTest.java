package com.example.library_system.scheduling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class QuartzExecutionBehaviourTest {

    private Scheduler scheduler;

    @BeforeEach
    void startScheduler() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", "test-scheduler-" + UUID.randomUUID());
        properties.setProperty("org.quartz.threadPool.threadCount", "3");
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        scheduler = new StdSchedulerFactory(properties).getScheduler();
        scheduler.start();

        SlowSerialJob.reset();
        SlowParallelJob.reset();
        FailingJob.EXECUTIONS.set(0);
    }

    @AfterEach
    void stopScheduler() throws Exception {
        scheduler.shutdown(true);
    }

    @Test
    void disallowConcurrentExecution_runLongerThanInterval_nextRunWaitsForPreviousOne() throws Exception {

        scheduleEvery100Ms(SlowSerialJob.class, "serial");


        await().atMost(Duration.ofSeconds(5)).until(() -> SlowSerialJob.EXECUTIONS.get() >= 3);

        assertThat(SlowSerialJob.MAX_RUNNING.get()).isEqualTo(1);
    }

    @Test
    void withoutDisallowConcurrentExecution_runLongerThanInterval_runsOverlap() throws Exception {

        scheduleEvery100Ms(SlowParallelJob.class, "parallel");

        await().atMost(Duration.ofSeconds(5)).until(() -> SlowParallelJob.EXECUTIONS.get() >= 3);

        assertThat(SlowParallelJob.MAX_RUNNING.get()).isGreaterThan(1);
    }

    @Test
    void failingJob_withoutImmediateRefire_runsOnceAndIsNotRetriedInALoop() throws Exception {

        JobDetail job = JobBuilder.newJob(FailingJob.class).withIdentity("failing", "test").build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("failing-once", "test").startNow().build();


        scheduler.scheduleJob(job, trigger);

        await().atMost(Duration.ofSeconds(5)).until(() -> FailingJob.EXECUTIONS.get() == 1);
        await().during(Duration.ofMillis(500)).atMost(Duration.ofSeconds(2))
                .until(() -> FailingJob.EXECUTIONS.get() == 1);
    }

    private void scheduleEvery100Ms(Class<? extends Job> jobClass, String name) throws Exception {
        JobDetail job = JobBuilder.newJob(jobClass).withIdentity(name, "test").build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name + "-trigger", "test")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(1)
                        .withIntervalInMilliseconds(100))
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    // Тестові задачі

    @DisallowConcurrentExecution
    public static class SlowSerialJob implements Job {

        static final AtomicInteger RUNNING = new AtomicInteger();
        static final AtomicInteger MAX_RUNNING = new AtomicInteger();
        static final AtomicInteger EXECUTIONS = new AtomicInteger();

        static void reset() {
            RUNNING.set(0);
            MAX_RUNNING.set(0);
            EXECUTIONS.set(0);
        }

        @Override
        public void execute(JobExecutionContext context) {
            simulateLongRun(RUNNING, MAX_RUNNING, EXECUTIONS);
        }
    }

    public static class SlowParallelJob implements Job {

        static final AtomicInteger RUNNING = new AtomicInteger();
        static final AtomicInteger MAX_RUNNING = new AtomicInteger();
        static final AtomicInteger EXECUTIONS = new AtomicInteger();

        static void reset() {
            RUNNING.set(0);
            MAX_RUNNING.set(0);
            EXECUTIONS.set(0);
        }

        @Override
        public void execute(JobExecutionContext context) {
            simulateLongRun(RUNNING, MAX_RUNNING, EXECUTIONS);
        }
    }

    public static class FailingJob implements Job {

        static final AtomicInteger EXECUTIONS = new AtomicInteger();

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            EXECUTIONS.incrementAndGet();
            throw new JobExecutionException(new IllegalStateException("Збій"), false);
        }
    }

    private static void simulateLongRun(AtomicInteger running, AtomicInteger maxRunning, AtomicInteger executions) {
        int nowRunning = running.incrementAndGet();
        maxRunning.accumulateAndGet(nowRunning, Math::max);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            running.decrementAndGet();
            executions.incrementAndGet();
        }
    }
}