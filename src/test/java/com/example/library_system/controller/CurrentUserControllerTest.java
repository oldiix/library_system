package com.example.library_system.controller;

import com.example.library_system.web.CurrentUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrentUserControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CurrentUserController())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
                .build();
    }

    @Test
    void me_adminUser_returnsUsernameRolesAndCatalogManagementAllowed() throws Exception {

        Authentication alice = UsernamePasswordAuthenticationToken.authenticated("alice", null, List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")));

        ResultActions result = mockMvc.perform(get("/api/me").principal(alice));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.roles[1]").value("USER"))
                .andExpect(jsonPath("$.canManageCatalog").value(true));
    }

    @Test
    void me_regularUser_returnsCatalogManagementNotAllowed() throws Exception {

        Authentication bob = UsernamePasswordAuthenticationToken.authenticated("bob", null, List.of(
                new SimpleGrantedAuthority("ROLE_USER")));

        ResultActions result = mockMvc.perform(get("/api/me").principal(bob));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.canManageCatalog").value(false));
    }

    @Test
    void me_requestWithoutUser_returns401() throws Exception {

        ResultActions result = mockMvc.perform(get("/api/me"));

        result.andExpect(status().isUnauthorized());
    }
}