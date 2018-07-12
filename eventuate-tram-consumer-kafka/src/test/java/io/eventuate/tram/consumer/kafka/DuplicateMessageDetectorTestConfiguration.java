package io.eventuate.tram.consumer.kafka;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramConsumerKafkaConfiguration.class)
@EnableAutoConfiguration
public class DuplicateMessageDetectorTestConfiguration {
}
