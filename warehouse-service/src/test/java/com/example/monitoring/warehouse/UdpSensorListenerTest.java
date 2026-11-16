package com.example.monitoring.warehouse;

import com.example.monitoring.common.SensorType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class UdpSensorListenerTest {

    private final RecordingPublisher publisher = new RecordingPublisher();
    private UdpSensorListener listener;

    @BeforeEach
    void start() {
        WarehouseProperties properties = new WarehouseProperties("warehouse-udp",
                Map.of(SensorType.TEMPERATURE, 0, SensorType.HUMIDITY, 0));
        IngestionService ingestion = new IngestionService(new MeasurementParser(), publisher, properties);
        listener = new UdpSensorListener(properties, ingestion);
        listener.start();
    }

    @AfterEach
    void stop() {
        listener.stop();
    }

    @Test
    void portDecidesSensorType() throws Exception {
        send("sensor_id=t1; value=36", listener.boundPort(SensorType.TEMPERATURE));
        send("sensor_id=h1; value=51", listener.boundPort(SensorType.HUMIDITY));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(publisher.published)
                        .extracting(m -> m.sensorId() + "/" + m.type())
                        .containsExactlyInAnyOrder("t1/TEMPERATURE", "h1/HUMIDITY"));
    }

    @Test
    void invalidDatagramDoesNotStopListener() throws Exception {
        int port = listener.boundPort(SensorType.TEMPERATURE);
        send("this is not valid", port);
        send("sensor_id=t1; value=20", port);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(publisher.published).hasSize(1));
    }

    private static void send(String payload, int port) throws Exception {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(new DatagramPacket(bytes, bytes.length, InetAddress.getLoopbackAddress(), port));
        }
    }
}
