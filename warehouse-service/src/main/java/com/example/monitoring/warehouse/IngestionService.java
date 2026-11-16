package com.example.monitoring.warehouse;

import com.example.monitoring.common.Measurement;
import com.example.monitoring.common.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final MeasurementParser parser;
    private final MeasurementPublisher publisher;
    private final WarehouseProperties properties;

    public IngestionService(MeasurementParser parser, MeasurementPublisher publisher, WarehouseProperties properties) {
        this.parser = parser;
        this.publisher = publisher;
        this.properties = properties;
    }

    public Mono<Void> ingest(String datagram, SensorType type) {
        Measurement measurement;
        try {
            measurement = parser.parse(datagram, type, properties.id());
        } catch (InvalidMeasurementException e) {
            log.warn("Dropping invalid {} reading: {}", type, e.getMessage());
            return Mono.empty();
        }
        return publisher.publish(measurement)
                .doOnSuccess(ignored -> log.info("Published {}", measurement))
                .onErrorResume(e -> {
                    log.error("Failed to publish {}", measurement, e);
                    return Mono.empty();
                });
    }
}
