package io.eventuate.tram.e2e.tests.rabbitmq.events;

import io.eventuate.e2e.tests.basic.events.AbstractTramEventTestConfiguration;
import io.eventuate.jdbcrabbitmq.TramJdbcRabbitMQConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({AbstractTramEventTestConfiguration.class, TramJdbcRabbitMQConfiguration.class})
public class JdbcRabbitMQTramEventTestConfiguration {
}
