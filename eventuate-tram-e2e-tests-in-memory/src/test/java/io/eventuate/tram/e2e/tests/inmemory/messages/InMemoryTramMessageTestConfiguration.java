package io.eventuate.tram.e2e.tests.inmemory.messages;

import io.eventuate.tram.inmemory.TramInMemoryConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramInMemoryConfiguration.class})
public class InMemoryTramMessageTestConfiguration {
}
