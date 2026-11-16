package com.example.monitoring.warehouse;

import com.example.monitoring.common.Measurement;
import com.example.monitoring.common.SensorType;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IngestionServiceTest {

    private final RecordingPublisher publisher = new RecordingPublisher();
    private final IngestionService service = new IngestionService(
            new MeasurementParser(), publisher, new WarehouseProperties("warehouse-test", Map.of()));

    @Test
    void publishesValidReadingWithWarehouseId() {
        StepVerifier.create(service.ingest("sensor_id=t1; value=30", SensorType.TEMPERATURE))
                .verifyComplete();

        assertThat(publisher.published)
                .containsExactly(new Measurement("warehouse-test", "t1", SensorType.TEMPERATURE, 30.0));
    }

    @Test
    void dropsInvalidReading() {
        StepVerifier.create(service.ingest("not-a-reading", SensorType.TEMPERATURE))
                .verifyComplete();

        assertThat(publisher.published).isEmpty();
    }

    @Test
    void publishFailureDoesNotFailTheStream() {
        publisher.failing = true;

        StepVerifier.create(service.ingest("sensor_id=t1; value=30", SensorType.TEMPERATURE))
                .verifyComplete();

        assertThat(publisher.published).isEmpty();
    }
}
