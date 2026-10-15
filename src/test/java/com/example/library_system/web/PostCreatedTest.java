package com.example.library_system.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostCreatedTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ComparisonController()).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/comparison/original", "/comparison/composed"})
    void post_originalAndComposedAnnotations_bothReturn201WithHandlerResult(String path) throws Exception {

        ResultActions result = mockMvc.perform(post(path));

        result.andExpect(status().isCreated())
                .andExpect(content().string("created"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/comparison/original", "/comparison/composed"})
    void get_originalAndComposedAnnotations_bothReturn405BecauseOnlyPostIsMapped(String path) throws Exception {

        ResultActions result = mockMvc.perform(get(path));

        result.andExpect(status().isMethodNotAllowed());
    }


    @Test
    void postCreated_mergedAnnotationAttributes_matchOriginalAnnotations() throws NoSuchMethodException {

        Method originalMethod = ComparisonController.class.getMethod("original");
        Method composedMethod = ComparisonController.class.getMethod("composed");

        RequestMapping originalMapping = AnnotatedElementUtils.findMergedAnnotation(originalMethod, RequestMapping.class);
        RequestMapping composedMapping = AnnotatedElementUtils.findMergedAnnotation(composedMethod, RequestMapping.class);
        ResponseStatus originalStatus = AnnotatedElementUtils.findMergedAnnotation(originalMethod, ResponseStatus.class);
        ResponseStatus composedStatus = AnnotatedElementUtils.findMergedAnnotation(composedMethod, ResponseStatus.class);

        assertThat(composedMapping).isNotNull();
        assertThat(composedMapping.method())
                .isEqualTo(originalMapping.method())
                .containsExactly(RequestMethod.POST);
        assertThat(composedMapping.path()).containsExactly("/composed");

        assertThat(composedStatus).isNotNull();
        assertThat(composedStatus.code())
                .isEqualTo(originalStatus.code())
                .isEqualTo(HttpStatus.CREATED);
    }


    @RestController
    @RequestMapping("/comparison")
    static class ComparisonController {

        @PostMapping("/original")
        @ResponseStatus(HttpStatus.CREATED)
        public String original() {
            return "created";
        }

        @PostCreated("/composed")
        public String composed() {
            return "created";
        }
    }
}