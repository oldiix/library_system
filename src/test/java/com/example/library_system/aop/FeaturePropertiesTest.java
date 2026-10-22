package com.example.library_system.aop;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeaturePropertiesTest {

    @Test
    void isEnabled_featureSetToTrue_returnsTrue() {

        FeatureProperties properties = new FeatureProperties(Map.of("book-deletion", true));

        boolean enabled = properties.isEnabled("book-deletion");

        assertThat(enabled).isTrue();
    }

    @Test
    void isEnabled_featureSetToFalse_returnsFalse() {

        FeatureProperties properties = new FeatureProperties(Map.of("book-deletion", false));

        boolean enabled = properties.isEnabled("book-deletion");

        assertThat(enabled).isFalse();
    }

    @Test
    void isEnabled_noFeaturesSectionInSettings_returnsFalse() {

        FeatureProperties properties = new FeatureProperties(null);

        boolean enabled = properties.isEnabled("book-deletion");

        assertThat(enabled).isFalse();
    }
}