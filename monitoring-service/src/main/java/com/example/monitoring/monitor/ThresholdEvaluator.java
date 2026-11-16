package com.example.monitoring.monitor;

import com.example.monitoring.common.Measurement;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class ThresholdEvaluator {

    private final MonitoringProperties properties;

    public ThresholdEvaluator(MonitoringProperties properties) {
        this.properties = properties;
    }

    public Optional<String> evaluate(Measurement m) {
        Double threshold = properties.thresholds().get(m.type());
        if (threshold == null || m.value() <= threshold) {
            return Optional.empty();
        }
        String unit = m.type().unit();
        return Optional.of(String.format(Locale.ROOT, "ALARM [%s] %s sensor %s = %.1f%s exceeds threshold %.1f%s",
                m.warehouseId(), m.type(), m.sensorId(), m.value(), unit, threshold, unit));
    }
}
