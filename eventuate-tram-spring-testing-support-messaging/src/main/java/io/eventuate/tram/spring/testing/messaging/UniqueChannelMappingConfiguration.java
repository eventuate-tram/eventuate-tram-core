package io.eventuate.tram.spring.testing.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UniqueChannelMappingConfiguration {

    @Bean
    public UniqueChannelMapping uniqueChannelMapping() {
        return new UniqueChannelMapping();
    }
}
