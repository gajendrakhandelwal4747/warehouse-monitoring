package com.example.monitoring.warehouse;

import com.example.monitoring.common.SensorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import reactor.netty.Connection;
import reactor.netty.udp.UdpServer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

@Component
public class UdpSensorListener implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(UdpSensorListener.class);

    private final WarehouseProperties properties;
    private final IngestionService ingestion;
    private final Map<SensorType, Connection> connections = new EnumMap<>(SensorType.class);

    public UdpSensorListener(WarehouseProperties properties, IngestionService ingestion) {
        this.properties = properties;
        this.ingestion = ingestion;
    }

    @Override
    public void start() {
        properties.udpPorts().forEach((type, port) -> {
            Connection connection = UdpServer.create()
                    .host("0.0.0.0")
                    .port(port)
                    .handle((in, out) -> in.receive()
                            .asString(StandardCharsets.UTF_8)
                            .concatMap(datagram -> ingestion.ingest(datagram, type))
                            .then())
                    .bindNow();
            connections.put(type, connection);
            log.info("Warehouse '{}' listening for {} readings on UDP port {}", properties.id(), type, boundPort(type));
        });
    }

    @Override
    public void stop() {
        connections.values().forEach(Connection::disposeNow);
        connections.clear();
    }

    @Override
    public boolean isRunning() {
        return !connections.isEmpty();
    }

    public int boundPort(SensorType type) {
        return ((InetSocketAddress) connections.get(type).channel().localAddress()).getPort();
    }
}
