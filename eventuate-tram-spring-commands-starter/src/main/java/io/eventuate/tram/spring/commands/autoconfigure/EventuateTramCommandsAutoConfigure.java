package io.eventuate.tram.spring.commands.autoconfigure;

import io.eventuate.tram.spring.commands.consumer.TramCommandConsumerConfiguration;
import io.eventuate.tram.spring.commands.producer.TramCommandProducerConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(TramCommandConsumerConfiguration.class)
@Import({TramCommandConsumerConfiguration.class, TramCommandProducerConfiguration.class})
public class EventuateTramCommandsAutoConfigure {
}
