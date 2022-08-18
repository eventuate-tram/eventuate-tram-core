package io.eventuate.tram.spring.commands.consumer;

import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramCommandReplyProducerConfiguration {
    @Bean
    public CommandReplyProducer commandReplyProducer(MessageProducer messageProducer) {
        return new CommandReplyProducer(messageProducer);
    }
}
