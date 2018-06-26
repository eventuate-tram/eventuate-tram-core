package io.eventuate.tram.consumer.kafka;

import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramConsumerKafkaConfiguration.class)
@EnableAutoConfiguration
@EnableConfigurationProperties({EventuateKafkaConsumerConfigurationProperties.class,
        EventuateKafkaProducerConfigurationProperties.class})
public class DuplicateMessageDetectorTestConfiguration {
}
