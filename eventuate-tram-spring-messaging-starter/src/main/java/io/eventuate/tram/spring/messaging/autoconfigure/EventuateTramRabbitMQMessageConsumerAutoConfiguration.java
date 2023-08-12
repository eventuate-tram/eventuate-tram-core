package io.eventuate.tram.spring.messaging.autoconfigure;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.rabbitmq.EventuateTramRabbitMQMessageConsumerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(EventuateTramRabbitMQMessageConsumerConfiguration.class)
@ConditionalOnMissingBean(MessageConsumerImplementation.class)
@Import(EventuateTramRabbitMQMessageConsumerConfiguration.class)
public class EventuateTramRabbitMQMessageConsumerAutoConfiguration {
}
