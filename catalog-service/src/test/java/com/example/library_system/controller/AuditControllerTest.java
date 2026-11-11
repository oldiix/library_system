package com.example.library_system.controller;

import com.example.library_system.audit.AuditEntry;
import com.example.library_system.audit.AuditTrail;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditControllerTest {

    @Test
    void findAll_entriesInTrail_returnsThemAsJson() throws Exception {

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.record(new AuditEntry(Instant.parse("2026-10-22T10:00:00Z"),
                "alice", "BOOK_DELETED", "BookService.delete(..)", "[5]"));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuditController(auditTrail)).build();

        ResultActions result = mockMvc.perform(get("/api/audit"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].action").value("BOOK_DELETED"))
                .andExpect(jsonPath("$[0].arguments").value("[5]"));
    }
}