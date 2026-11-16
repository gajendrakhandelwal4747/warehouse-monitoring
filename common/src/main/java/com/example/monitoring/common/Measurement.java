package com.example.monitoring.common;

public record Measurement(String warehouseId, String sensorId, SensorType type, double value) {
}
