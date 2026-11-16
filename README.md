# Warehouse Monitoring

Sensors send temperature and humidity readings over UDP to the warehouse service. It publishes
them to Kafka, and the central monitoring service logs an alarm when a reading goes over its
threshold.

Stack: Java 21, Spring Boot 3, Reactor Netty (UDP), Spring Kafka, Kafka.

```
sensors --UDP--> warehouse-service --Kafka (sensor-measurements)--> monitoring-service --> ALARM in log
```

Modules:

- `warehouse-service` - listens on UDP 3344 (temperature) and 3355 (humidity), parses
  `sensor_id=t1; value=30` and publishes the reading to Kafka with the warehouse id.
- `monitoring-service` - reads from Kafka and logs an alarm when temperature > 35°C or
  humidity > 50%.
- `common` - the `Measurement` record and JSON helper used by both services.

Every reading carries a warehouse id, so the monitoring service can handle many warehouses.
Start another `warehouse-service` with a different `WAREHOUSE_ID` to add one.

## How to test

Prerequisites: Docker with Compose v2 and netcat (`nc`). For the unit tests also Java 21 and Maven.

### 1. Unit tests

```bash
mvn test
```

Covers the parser, the threshold check, ingestion error handling and the UDP listener on real
sockets. Expected result: `BUILD SUCCESS`.

### 2. Start the system

```bash
docker compose up -d --build
```

The first build downloads dependencies and takes a few minutes. Wait until both services are up:

```bash
docker compose logs warehouse-service monitoring-service | grep -E "listening|partitions assigned"
```

You should see `listening for TEMPERATURE readings on UDP port 3344`, `listening for HUMIDITY
readings on UDP port 3355` and `partitions assigned: [sensor-measurements-0]`.

### 3. Watch the alarm log

In a second terminal:

```bash
docker compose logs -f monitoring-service
```

### 4. Send readings

| Command | Expected in monitoring-service log |
|---|---|
| `echo "sensor_id=t1; value=40" \| nc -u -w1 localhost 3344` | `ALARM [warehouse-1] TEMPERATURE sensor t1 = 40.0°C exceeds threshold 35.0°C` |
| `echo "sensor_id=t1; value=30" \| nc -u -w1 localhost 3344` | nothing |
| `echo "sensor_id=t1; value=35" \| nc -u -w1 localhost 3344` | nothing (35 is not above 35) |
| `echo "sensor_id=h1; value=55" \| nc -u -w1 localhost 3355` | `ALARM [warehouse-1] HUMIDITY sensor h1 = 55.0% exceeds threshold 50.0%` |
| `echo "sensor_id=h1; value=40" \| nc -u -w1 localhost 3355` | nothing |

### 5. Send bad input

```bash
echo "this is garbage" | nc -u -w1 localhost 3344
docker compose logs warehouse-service | grep Dropping
```

Expected: `Dropping invalid TEMPERATURE reading: ...`. The service keeps running, so readings
sent after this still work.

### 6. Stop

```bash
docker compose down -v
```

## Configuration

| Setting | Default | Env var |
|---|---|---|
| Warehouse id | `warehouse-1` | `WAREHOUSE_ID` |
| Temperature threshold | `35` | `MONITORING_THRESHOLDS_TEMPERATURE` |
| Humidity threshold | `50` | `MONITORING_THRESHOLDS_HUMIDITY` |
| Kafka | `localhost:29092` | `KAFKA_BOOTSTRAP_SERVERS` |

## Notes

- The message has no type field, so the sensor type comes from the port it arrived on.
- "Exceeds" means strictly greater: 35.0 doesn't alarm, 35.1 does.
- Bad packets are logged and dropped, the listener keeps running.
