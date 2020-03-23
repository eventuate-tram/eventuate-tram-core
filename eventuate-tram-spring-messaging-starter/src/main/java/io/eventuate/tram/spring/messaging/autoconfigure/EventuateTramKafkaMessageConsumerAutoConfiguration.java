package io.eventuate.tram.spring.messaging.autoconfigure;

import io.eventuate.tram.spring.consumer.kafka.EventuateTramKafkaMessageConsumerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(EventuateTramKafkaMessageConsumerConfiguration.class)
@Import(EventuateTramKafkaMessageConsumerConfiguration.class)
public class EventuateTramKafkaMessageConsumerAutoConfiguration {
}
