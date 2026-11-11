package com.example.library_system.dto;

import java.util.List;

public record CurrentUserResponse(String username, List<String> roles, boolean canManageCatalog) {
}