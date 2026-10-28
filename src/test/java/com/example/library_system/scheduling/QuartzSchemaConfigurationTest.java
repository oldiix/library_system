package com.example.library_system.scheduling;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class QuartzSchemaConfigurationTest {

    private static final String[] QUARTZ_TABLES = {
            "QRTZ_JOB_DETAILS", "QRTZ_TRIGGERS", "QRTZ_SIMPLE_TRIGGERS", "QRTZ_CRON_TRIGGERS",
            "QRTZ_SIMPROP_TRIGGERS", "QRTZ_BLOB_TRIGGERS", "QRTZ_CALENDARS", "QRTZ_PAUSED_TRIGGER_GRPS",
            "QRTZ_FIRED_TRIGGERS", "QRTZ_SCHEDULER_STATE", "QRTZ_LOCKS"
    };

    @Test
    void quartzSqlScript_containsAllQuartzTables() throws IOException {

        String sql = readResource("db/changelog/sql/quartz-tables-mysql.sql");

        for (String table : QUARTZ_TABLES) {
            assertThat(sql).contains("CREATE TABLE " + table + " (");
        }
    }

    @Test
    void masterChangelog_includesQuartzChangeset() throws IOException {

        String master = readResource("db/changelog/db.changelog-master.yaml");
        String changeset = readResource("db/changelog/changes/004-create-quartz-tables.yaml");

        assertThat(master).contains("db/changelog/changes/004-create-quartz-tables.yaml");
        assertThat(changeset)
                .contains("sqlFile")
                .contains("db/changelog/sql/quartz-tables-mysql.sql");
    }

    @Test
    void applicationSettings_jdbcJobStoreWithoutSpringSchemaInitialization() {

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));

        Properties properties = yaml.getObject();


        assertThat(properties).isNotNull();
        assertThat(properties.getProperty("spring.quartz.job-store-type")).isEqualTo("jdbc");
        assertThat(properties.getProperty("spring.quartz.jdbc.initialize-schema")).isEqualTo("never");
        assertThat(properties.getProperty("spring.quartz.overwrite-existing-jobs")).isEqualTo("false");
        assertThat(properties.getProperty("spring.quartz.properties.org.quartz.jobStore.tablePrefix"))
                .isEqualTo("QRTZ_");
    }

    private static String readResource(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}