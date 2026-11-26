package com.example.library_system.partner;

public class PartnerDataNotFoundException extends RuntimeException {

    public PartnerDataNotFoundException(String message) {
        super(message);
    }

    public PartnerDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}