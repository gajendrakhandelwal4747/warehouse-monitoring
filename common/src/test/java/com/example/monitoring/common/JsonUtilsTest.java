package com.example.monitoring.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonUtilsTest {

    @Test
    void measurementRoundTrips() {
        Measurement original = new Measurement("warehouse-1", "t1", SensorType.TEMPERATURE, 30.5);

        assertThat(JsonUtils.fromJson(JsonUtils.toJson(original), Measurement.class)).isEqualTo(original);
    }

    @Test
    void invalidJsonFailsWithIllegalArgument() {
        assertThatThrownBy(() -> JsonUtils.fromJson("not json", Measurement.class))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
