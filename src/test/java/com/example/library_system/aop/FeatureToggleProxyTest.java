package com.example.library_system.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeatureToggleProxyTest {

    private SampleService target;
    private SampleService proxy;

    @BeforeEach
    void setUp() {
        target = new SampleService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new FeatureToggleAspect(new FeatureProperties(Map.of(
                "enabled-feature", true,
                "disabled-feature", false))));
        proxy = factory.getProxy();
    }

    @Test
    void callThroughProxy_enabledFeature_methodExecutes() {

        String result = proxy.enabledAction();

        assertThat(result).isEqualTo("done");
        assertThat(target.executions).isEqualTo(1);
    }

    @Test
    void callThroughProxy_disabledFeature_adviceBlocksMethod() {

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> proxy.disabledAction());

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(target.executions).isZero();
    }

    static class SampleService {

        int executions = 0;

        @FeatureToggle("enabled-feature")
        public String enabledAction() {
            executions++;
            return "done";
        }

        @FeatureToggle("disabled-feature")
        public String disabledAction() {
            executions++;
            return "done";
        }
    }
}