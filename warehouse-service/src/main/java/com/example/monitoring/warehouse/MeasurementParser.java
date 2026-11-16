package com.example.monitoring.warehouse;

import com.example.monitoring.common.Measurement;
import com.example.monitoring.common.SensorType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class MeasurementParser {

    private static final Pattern NUMBER = Pattern.compile("[-+]?\\d+(\\.\\d+)?");

    public Measurement parse(String raw, SensorType type, String warehouseId) {
        Map<String, String> fields = new HashMap<>();
        for (String part : raw.trim().split(";")) {
            if (part.isBlank()) {
                continue;
            }
            String[] keyValue = part.split("=", 2);
            if (keyValue.length != 2) {
                throw new InvalidMeasurementException("Malformed field '" + part.trim() + "' in: " + raw);
            }
            fields.put(keyValue[0].trim().toLowerCase(Locale.ROOT), keyValue[1].trim());
        }

        String sensorId = fields.get("sensor_id");
        if (sensorId == null || sensorId.isBlank()) {
            throw new InvalidMeasurementException("Missing sensor_id in: " + raw);
        }
        String value = fields.get("value");
        if (value == null || !NUMBER.matcher(value).matches()) {
            throw new InvalidMeasurementException("Missing or non-numeric value in: " + raw);
        }

        return new Measurement(warehouseId, sensorId, type, Double.parseDouble(value));
    }
}
