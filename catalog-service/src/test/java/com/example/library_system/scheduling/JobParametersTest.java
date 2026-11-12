package com.example.library_system.scheduling;

import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobParametersTest {

    @Test
    void requiredInt_keyMissing_throwsExceptionNamingTheKey() {

        JobDataMap data = new JobDataMap();

        JobExecutionException exception = assertThrows(JobExecutionException.class,
                () -> JobParameters.requiredInt(data, "batchSize"));

        assertThat(exception.getMessage()).contains("Відсутній обов'язковий параметр", "'batchSize'");
        assertThat(exception.refireImmediately()).isFalse();
    }

    @Test
    void requiredInt_valueIsNotANumber_throwsExceptionWithReceivedValue() {

        JobDataMap data = new JobDataMap(Map.of("threshold", "три"));

        JobExecutionException exception = assertThrows(JobExecutionException.class,
                () -> JobParameters.requiredInt(data, "threshold"));

        assertThat(exception.getMessage()).contains("'threshold'", "цілим числом", "'три'");
    }

    @Test
    void requiredInt_numberStoredAsText_isParsed() throws JobExecutionException {

        JobDataMap data = new JobDataMap(Map.of("batchSize", " 50 "));

        int value = JobParameters.requiredInt(data, "batchSize");

        assertThat(value).isEqualTo(50);
    }

    @Test
    void requiredString_blankValue_throwsExceptionNamingTheKey() {

        JobDataMap data = new JobDataMap(Map.of("reportName", "   "));

        JobExecutionException exception = assertThrows(JobExecutionException.class,
                () -> JobParameters.requiredString(data, "reportName"));

        assertThat(exception.getMessage()).contains("'reportName'");
    }
}