package com.example.library_system.web;

import java.util.List;

public record AuthenticatedUser(String username, List<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}