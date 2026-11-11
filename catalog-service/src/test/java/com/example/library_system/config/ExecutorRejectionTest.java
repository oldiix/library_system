package com.example.library_system.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutorRejectionTest {

    @Test
    void execute_moreTasksThanPoolAndQueueCanHold_extraTaskIsRejected() {

        ThreadPoolTaskExecutor executor = AsyncConfig.newExecutor(new AsyncProperties(1, 1, 1, "tiny-async-"));
        executor.initialize();
        CountDownLatch release = new CountDownLatch(1);
        Runnable blockingTask = () -> {
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        try {
            executor.execute(blockingTask);   // задача 1: займає єдиний потік
            executor.execute(blockingTask);   // задача 2: чекає в черзі

            TaskRejectedException exception = assertThrows(TaskRejectedException.class,
                    () -> executor.execute(blockingTask));

            assertThat(exception).hasMessageContaining("did not accept task");
        } finally {
            release.countDown();
            executor.shutdown();
        }
    }
}