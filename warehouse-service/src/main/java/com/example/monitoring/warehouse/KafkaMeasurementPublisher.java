package com.example.monitoring.warehouse;

import com.example.monitoring.common.JsonUtils;
import com.example.monitoring.common.Measurement;
import com.example.monitoring.common.Topics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class KafkaMeasurementPublisher implements MeasurementPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaMeasurementPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> publish(Measurement measurement) {
        return Mono.fromFuture(() -> kafkaTemplate.send(Topics.SENSOR_MEASUREMENTS, JsonUtils.toJson(measurement)))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
