package com.example.library_system.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
final class JobFailurePolicy {

    private JobFailurePolicy() {
    }

    static JobExecutionException recordAndWaitForNextRun(JobExecutionContext context, Exception cause) {
        log.error("Задача {} (тригер {}) завершилась з помилкою: {}. "
                        + "Негайного повтору не буде — наступна спроба за розкладом.",
                context.getJobDetail().getKey(), context.getTrigger().getKey(), cause.getMessage(), cause);

        JobExecutionException exception = new JobExecutionException(cause, false);
        exception.setRefireImmediately(false);
        exception.setUnscheduleAllTriggers(false);
        return exception;
    }
}