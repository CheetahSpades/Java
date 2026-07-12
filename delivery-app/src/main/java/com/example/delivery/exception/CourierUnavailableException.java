package com.example.delivery.exception;

public class CourierUnavailableException extends RuntimeException {

    public CourierUnavailableException(String message) {
        super(message);
    }
}
