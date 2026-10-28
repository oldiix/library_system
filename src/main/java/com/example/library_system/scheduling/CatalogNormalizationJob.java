package com.example.library_system.scheduling;

import com.example.library_system.service.CatalogNormalizationService;
import com.example.library_system.service.NormalizationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class CatalogNormalizationJob implements Job {

    public static final String BATCH_SIZE = "batchSize";

    private final CatalogNormalizationService catalogNormalizationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int batchSize = JobParameters.requiredInt(context.getMergedJobDataMap(), BATCH_SIZE);

        NormalizationResult result;
        try {
            result = catalogNormalizationService.normalizeCatalog(batchSize);
        } catch (RuntimeException exception) {
            throw JobFailurePolicy.recordAndWaitForNextRun(context, exception);
        }

        log.info("Задача {} (тригер {}) завершена: змінено записів — {}",
                context.getJobDetail().getKey(), context.getTrigger().getKey(), result.total());
    }
}