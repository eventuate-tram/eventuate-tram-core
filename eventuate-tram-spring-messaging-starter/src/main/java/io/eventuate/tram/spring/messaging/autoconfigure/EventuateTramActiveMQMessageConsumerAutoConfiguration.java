package io.eventuate.tram.spring.messaging.autoconfigure;

import io.eventuate.tram.consumer.activemq.EventuateTramActiveMQMessageConsumerConfiguration;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(EventuateTramActiveMQMessageConsumerConfiguration.class)
@ConditionalOnMissingBean(MessageConsumerImplementation.class)
@Import(EventuateTramActiveMQMessageConsumerConfiguration.class)
public class EventuateTramActiveMQMessageConsumerAutoConfiguration {
}
