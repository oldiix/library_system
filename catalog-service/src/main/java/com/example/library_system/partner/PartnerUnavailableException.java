package com.example.library_system.partner;

public class PartnerUnavailableException extends RuntimeException {

    public PartnerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}