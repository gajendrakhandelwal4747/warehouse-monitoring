package com.example.monitoring.monitor;

import com.example.monitoring.common.SensorType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "monitoring")
public record MonitoringProperties(Map<SensorType, Double> thresholds) {
}
