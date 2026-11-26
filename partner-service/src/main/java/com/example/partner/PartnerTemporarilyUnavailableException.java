package com.example.partner;

public class PartnerTemporarilyUnavailableException extends RuntimeException {

    public PartnerTemporarilyUnavailableException(String message) {
        super(message);
    }
}