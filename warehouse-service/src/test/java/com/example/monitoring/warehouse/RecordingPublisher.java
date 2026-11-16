package com.example.monitoring.warehouse;

import com.example.monitoring.common.Measurement;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class RecordingPublisher implements MeasurementPublisher {

    final List<Measurement> published = new CopyOnWriteArrayList<>();
    volatile boolean failing;

    @Override
    public Mono<Void> publish(Measurement measurement) {
        return Mono.defer(() -> failing
                ? Mono.error(new IllegalStateException("broker down"))
                : Mono.fromRunnable(() -> published.add(measurement)));
    }
}
