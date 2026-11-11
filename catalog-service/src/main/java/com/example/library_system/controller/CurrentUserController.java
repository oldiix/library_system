package com.example.library_system.controller;

import com.example.library_system.dto.CurrentUserResponse;
import com.example.library_system.web.AuthenticatedUser;
import com.example.library_system.web.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class CurrentUserController {

    @GetMapping
    public CurrentUserResponse me(@CurrentUser AuthenticatedUser user) {
        boolean canManageCatalog = user.hasRole("ADMIN");
        return new CurrentUserResponse(user.username(), user.roles(), canManageCatalog);
    }
}