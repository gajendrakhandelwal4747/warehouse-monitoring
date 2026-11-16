package com.example.monitoring.monitor;

import com.example.monitoring.common.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoringApplication.class, args);
    }

    @Bean
    NewTopic sensorMeasurementsTopic() {
        return TopicBuilder.name(Topics.SENSOR_MEASUREMENTS).build();
    }
}
