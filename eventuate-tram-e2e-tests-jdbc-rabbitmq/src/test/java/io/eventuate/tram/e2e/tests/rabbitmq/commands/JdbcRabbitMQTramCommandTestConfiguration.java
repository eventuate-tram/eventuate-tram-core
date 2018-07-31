package io.eventuate.tram.e2e.tests.rabbitmq.commands;

import io.eventuate.e2e.tests.basic.commands.AbstractTramCommandTestConfiguration;
import io.eventuate.jdbcrabbitmq.TramJdbcRabbitMQConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({AbstractTramCommandTestConfiguration.class, TramJdbcRabbitMQConfiguration.class, })
public class JdbcRabbitMQTramCommandTestConfiguration {
}
