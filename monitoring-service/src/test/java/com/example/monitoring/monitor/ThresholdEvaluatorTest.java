package com.example.monitoring.monitor;

import com.example.monitoring.common.Measurement;
import com.example.monitoring.common.SensorType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.example.monitoring.common.SensorType.HUMIDITY;
import static com.example.monitoring.common.SensorType.TEMPERATURE;
import static org.assertj.core.api.Assertions.assertThat;

class ThresholdEvaluatorTest {

    private final ThresholdEvaluator evaluator =
            new ThresholdEvaluator(new MonitoringProperties(Map.of(TEMPERATURE, 35.0, HUMIDITY, 50.0)));

    @Test
    void temperatureBelowThresholdRaisesNothing() {
        assertThat(evaluator.evaluate(reading("t1", TEMPERATURE, 30))).isEmpty();
    }

    @Test
    void exactlyAtThresholdRaisesNothing() {
        assertThat(evaluator.evaluate(reading("t1", TEMPERATURE, 35))).isEmpty();
    }

    @Test
    void temperatureAboveThresholdRaisesAlarm() {
        assertThat(evaluator.evaluate(reading("t1", TEMPERATURE, 40)))
                .contains("ALARM [warehouse-1] TEMPERATURE sensor t1 = 40.0°C exceeds threshold 35.0°C");
    }

    @Test
    void humidityBelowThresholdRaisesNothing() {
        assertThat(evaluator.evaluate(reading("h1", HUMIDITY, 40))).isEmpty();
    }

    @Test
    void humidityAboveThresholdRaisesAlarm() {
        assertThat(evaluator.evaluate(reading("h1", HUMIDITY, 55)))
                .contains("ALARM [warehouse-1] HUMIDITY sensor h1 = 55.0% exceeds threshold 50.0%");
    }

    private static Measurement reading(String sensor, SensorType type, double value) {
        return new Measurement("warehouse-1", sensor, type, value);
    }
}
