package com.example.library_system.controller;

import com.example.library_system.dto.BookDetailsResponse;
import com.example.library_system.service.BookDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookDetailsController {

    private final BookDetailsService bookDetailsService;

    @GetMapping("/{id}/details")
    public BookDetailsResponse getDetails(@PathVariable Long id) {
        return bookDetailsService.getDetails(id);
    }
}