package io.eventuate.tram.spring.testing.messaging.consumer;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssertableMessageConsumerConfiguration {

    @Bean
    public AssertableMessageConsumer testMessageConsumer(MessageConsumer messageConsumer) {
        return new AssertableMessageConsumer(messageConsumer);
    }

}
