package com.example.monitoring.warehouse;

public class InvalidMeasurementException extends RuntimeException {
    public InvalidMeasurementException(String message) {
        super(message);
    }
}
