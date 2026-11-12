package com.example.library_system.scheduling;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;

final class JobParameters {

    private JobParameters() {
    }

    static int requiredInt(JobDataMap data, String key) throws JobExecutionException {
        Object value = data.get(key);
        if (value == null) {
            throw new JobExecutionException("Відсутній обов'язковий параметр завдання '" + key + "'");
        }
        if (value instanceof Integer number) {
            return number;
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new JobExecutionException(
                    "Параметр завдання '" + key + "' має бути цілим числом, отримано: '" + value + "'", e);
        }
    }

    static String requiredString(JobDataMap data, String key) throws JobExecutionException {
        Object value = data.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new JobExecutionException("Відсутній обов'язковий параметр завдання '" + key + "'");
        }
        return value.toString();
    }
}