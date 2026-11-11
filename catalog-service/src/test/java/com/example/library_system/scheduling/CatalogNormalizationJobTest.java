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
import org.quartz.JobExecutionException;
import org.quartz.TriggerBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap(Map.of(
                CatalogNormalizationJob.BATCH_SIZE, 50)));
        stubJobAndTriggerKeys();
        when(catalogNormalizationService.normalizeCatalog(50)).thenReturn(new NormalizationResult(1, 0));
        CatalogNormalizationJob job = new CatalogNormalizationJob(catalogNormalizationService);

        job.execute(context);

        verify(catalogNormalizationService).normalizeCatalog(50);
        verifyNoMoreInteractions(catalogNormalizationService);
    }

    @Test
    void execute_serviceFails_errorRecordedWithoutImmediateRefire() {

        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap(Map.of(
                CatalogNormalizationJob.BATCH_SIZE, 50)));
        stubJobAndTriggerKeys();
        IllegalStateException failure = new IllegalStateException("База даних недоступна");
        when(catalogNormalizationService.normalizeCatalog(50)).thenThrow(failure);
        CatalogNormalizationJob job = new CatalogNormalizationJob(catalogNormalizationService);

        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> job.execute(context));

        assertThat(exception.getCause()).isSameAs(failure);
        assertThat(exception.refireImmediately()).isFalse();
        assertThat(exception.unscheduleAllTriggers()).isFalse();

        verify(catalogNormalizationService, times(1)).normalizeCatalog(50);
    }

    @Test
    void execute_batchSizeMissing_failsExplicitlyWithoutCallingService() {

        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());
        CatalogNormalizationJob job = new CatalogNormalizationJob(catalogNormalizationService);

        JobExecutionException exception = assertThrows(JobExecutionException.class, () -> job.execute(context));

        assertThat(exception.getMessage()).contains("'batchSize'");
        verifyNoInteractions(catalogNormalizationService);
    }

    private void stubJobAndTriggerKeys() {
        when(context.getJobDetail()).thenReturn(JobBuilder.newJob(CatalogNormalizationJob.class)
                .withIdentity(QuartzJobsConfig.NORMALIZATION_JOB_KEY).build());
        when(context.getTrigger()).thenReturn(TriggerBuilder.newTrigger()
                .withIdentity(QuartzJobsConfig.NORMALIZATION_NIGHTLY_TRIGGER_KEY).build());
    }
}