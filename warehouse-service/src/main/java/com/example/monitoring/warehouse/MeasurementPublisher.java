package com.example.monitoring.warehouse;

import com.example.monitoring.common.Measurement;
import reactor.core.publisher.Mono;

public interface MeasurementPublisher {
    Mono<Void> publish(Measurement measurement);
}
