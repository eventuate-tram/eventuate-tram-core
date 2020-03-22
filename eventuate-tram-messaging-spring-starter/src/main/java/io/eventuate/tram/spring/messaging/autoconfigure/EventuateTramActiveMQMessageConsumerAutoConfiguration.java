package io.eventuate.tram.spring.messaging.autoconfigure;

import io.eventuate.tram.consumer.activemq.EventuateTramActiveMQMessageConsumerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(EventuateTramActiveMQMessageConsumerConfiguration.class)
@Import(EventuateTramActiveMQMessageConsumerConfiguration.class)
public class EventuateTramActiveMQMessageConsumerAutoConfiguration {
}
