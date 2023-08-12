package io.eventuate.tram.spring.messaging.autoconfigure;

import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.redis.EventuateTramRedisMessageConsumerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(EventuateTramRedisMessageConsumerConfiguration.class)
@ConditionalOnMissingBean(MessageConsumerImplementation.class)
@Import(EventuateTramRedisMessageConsumerConfiguration.class)
public class EventuateTramRedisMessageConsumerAutoConfiguration {
}
