package com.example.library_system.service;

public record NormalizationResult(int booksUpdated, int authorsUpdated) {

    public int total() {
        return booksUpdated + authorsUpdated;
    }
}