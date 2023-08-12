package io.eventuate.tram.spring.reactive.commands.autoconfigure;

import io.eventuate.tram.spring.reactive.commands.consumer.ReactiveTramCommandConsumerConfiguration;
import io.eventuate.tram.spring.reactive.commands.producer.ReactiveTramCommandProducerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ReactiveTramCommandConsumerConfiguration.class, ReactiveTramCommandProducerConfiguration.class})
public class EventuateReactiveTramCommandsAutoConfigure {
}
