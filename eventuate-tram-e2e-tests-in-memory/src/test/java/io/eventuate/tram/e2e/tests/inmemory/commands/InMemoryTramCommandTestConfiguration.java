package io.eventuate.tram.e2e.tests.inmemory.commands;

import io.eventuate.e2e.tests.basic.commands.AbstractTramCommandTestConfiguration;
import io.eventuate.tram.inmemory.TramInMemoryConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({
        AbstractTramCommandTestConfiguration.class,
        TramInMemoryConfiguration.class,
})
public class InMemoryTramCommandTestConfiguration {
}
