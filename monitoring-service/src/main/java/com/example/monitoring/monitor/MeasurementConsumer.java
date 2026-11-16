package com.example.monitoring.monitor;

import com.example.monitoring.common.JsonUtils;
import com.example.monitoring.common.Measurement;
import com.example.monitoring.common.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MeasurementConsumer {

    private static final Logger log = LoggerFactory.getLogger(MeasurementConsumer.class);

    private final ThresholdEvaluator evaluator;

    public MeasurementConsumer(ThresholdEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @KafkaListener(topics = Topics.SENSOR_MEASUREMENTS)
    public void consume(String json) {
        try {
            Measurement measurement = JsonUtils.fromJson(json, Measurement.class);
            evaluator.evaluate(measurement).ifPresent(log::warn);
        } catch (IllegalArgumentException e) {
            log.warn("Skipping unreadable measurement: {}", e.getMessage());
        }
    }
}
