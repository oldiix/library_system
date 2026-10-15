package com.example.library_system.config;

import com.example.library_system.web.CurrentUserArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTest {

    @Test
    void addArgumentResolvers_emptyList_registersCurrentUserResolver() {

        WebConfig webConfig = new WebConfig();
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        webConfig.addArgumentResolvers(resolvers);

        assertThat(resolvers).hasSize(1);
        assertThat(resolvers.getFirst()).isInstanceOf(CurrentUserArgumentResolver.class);
    }
}