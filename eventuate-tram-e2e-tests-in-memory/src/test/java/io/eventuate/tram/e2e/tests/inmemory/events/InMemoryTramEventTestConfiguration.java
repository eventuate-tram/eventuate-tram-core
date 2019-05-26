package io.eventuate.tram.e2e.tests.inmemory.events;

import io.eventuate.e2e.tests.basic.events.AbstractTramEventTestConfiguration;
import io.eventuate.tram.inmemory.TramInMemoryConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({AbstractTramEventTestConfiguration.class, TramInMemoryConfiguration.class,})
class InMemoryTramEventTestConfiguration {
}
