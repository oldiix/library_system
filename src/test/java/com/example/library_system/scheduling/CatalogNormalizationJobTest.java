package com.example.library_system.scheduling;

import com.example.library_system.service.CatalogNormalizationService;
import com.example.library_system.service.NormalizationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.TriggerBuilder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogNormalizationJobTest {

    @Mock
    private CatalogNormalizationService catalogNormalizationService;

    @Mock
    private JobExecutionContext context;

    @Test
    void execute_batchSizeInMergedData_readsParameterAndCallsServiceExactlyOnce() throws Exception {

        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap(java.util.Map.of(
                CatalogNormalizationJob.BATCH_SIZE, 50)));
        when(context.getJobDetail()).thenReturn(JobBuilder.newJob(CatalogNormalizationJob.class)
                .withIdentity(QuartzJobsConfig.NORMALIZATION_JOB_KEY).build());
        when(context.getTrigger()).thenReturn(TriggerBuilder.newTrigger()
                .withIdentity(QuartzJobsConfig.NORMALIZATION_NIGHTLY_TRIGGER_KEY).build());
        when(catalogNormalizationService.normalizeCatalog(50)).thenReturn(new NormalizationResult(1, 0));
        CatalogNormalizationJob job = new CatalogNormalizationJob(catalogNormalizationService);

        job.execute(context);

        verify(catalogNormalizationService).normalizeCatalog(50);
        verifyNoMoreInteractions(catalogNormalizationService);
    }
}