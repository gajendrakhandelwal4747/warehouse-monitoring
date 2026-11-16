package com.example.monitoring.warehouse;

import com.example.monitoring.common.SensorType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "warehouse")
public record WarehouseProperties(String id, Map<SensorType, Integer> udpPorts) {
}
