package com.example.partner;

public class BookNotInCatalogException extends RuntimeException {

    public BookNotInCatalogException(String isbn) {
        super("Книги з ISBN " + isbn + " немає в каталозі партнера");
    }
}