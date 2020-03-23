package io.eventuate.tram.spring.messaging.autoconfigure;

import io.eventuate.tram.consumer.rabbitmq.EventuateTramRabbitMQMessageConsumerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(EventuateTramRabbitMQMessageConsumerConfiguration.class)
@Import(EventuateTramRabbitMQMessageConsumerConfiguration.class)
public class EventuateTramRabbitMQMessageConsumerAutoConfiguration {
}
