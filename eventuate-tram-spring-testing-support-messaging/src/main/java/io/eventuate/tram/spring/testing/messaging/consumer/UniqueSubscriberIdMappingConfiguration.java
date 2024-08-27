package io.eventuate.tram.spring.testing.messaging.consumer;

import io.eventuate.tram.messaging.consumer.SubscriberMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UniqueSubscriberIdMappingConfiguration {

    @Bean
    public SubscriberMapping subscriberMapping() {
        return new UniqueSubscriberIdMapping();
    }

}

