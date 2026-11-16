package com.example.monitoring.warehouse;

import com.example.monitoring.common.Measurement;
import com.example.monitoring.common.SensorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeasurementParserTest {

    private final MeasurementParser parser = new MeasurementParser();

    @Test
    void parsesTemperatureExampleFromSpec() {
        Measurement m = parser.parse("sensor_id=t1; value=30", SensorType.TEMPERATURE, "warehouse-1");

        assertThat(m).isEqualTo(new Measurement("warehouse-1", "t1", SensorType.TEMPERATURE, 30.0));
    }

    @Test
    void parsesHumidityExampleFromSpec() {
        Measurement m = parser.parse("sensor_id=h1; value=40", SensorType.HUMIDITY, "warehouse-1");

        assertThat(m).isEqualTo(new Measurement("warehouse-1", "h1", SensorType.HUMIDITY, 40.0));
    }

    @Test
    void toleratesWhitespaceAndTrailingNewline() {
        Measurement m = parser.parse(" sensor_id = t2 ; value= -3.5\n", SensorType.TEMPERATURE, "w");

        assertThat(m.sensorId()).isEqualTo("t2");
        assertThat(m.value()).isEqualTo(-3.5);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "garbage",
            "sensor_id=t1",
            "value=30",
            "sensor_id=; value=30",
            "sensor_id=t1; value=abc"
    })
    void rejectsMalformedInput(String raw) {
        assertThatThrownBy(() -> parser.parse(raw, SensorType.TEMPERATURE, "w"))
                .isInstanceOf(InvalidMeasurementException.class);
    }
}
