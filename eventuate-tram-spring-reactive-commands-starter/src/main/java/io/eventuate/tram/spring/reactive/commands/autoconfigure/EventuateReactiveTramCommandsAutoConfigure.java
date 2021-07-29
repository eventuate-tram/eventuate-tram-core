package io.eventuate.tram.spring.reactive.commands.autoconfigure;

import io.eventuate.tram.spring.reactive.commands.consumer.ReactiveTramCommandConsumerConfiguration;
import io.eventuate.tram.spring.reactive.commands.producer.ReactiveTramCommandProducerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(ReactiveTramCommandConsumerConfiguration.class)
@Import({ReactiveTramCommandConsumerConfiguration.class, ReactiveTramCommandProducerConfiguration.class})
public class EventuateReactiveTramCommandsAutoConfigure {
}
