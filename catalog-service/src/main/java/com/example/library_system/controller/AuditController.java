package com.example.library_system.controller;

import com.example.library_system.audit.AuditEntry;
import com.example.library_system.audit.AuditTrail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditTrail auditTrail;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditEntry> findAll() {
        return auditTrail.findAll();
    }
}