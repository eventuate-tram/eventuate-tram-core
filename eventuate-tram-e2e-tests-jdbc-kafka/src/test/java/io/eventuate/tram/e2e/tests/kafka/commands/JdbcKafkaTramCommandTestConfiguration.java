package io.eventuate.tram.e2e.tests.kafka.commands;

import io.eventuate.e2e.tests.basic.commands.AbstractTramCommandTestConfiguration;
import io.eventuate.jdbckafka.TramJdbcKafkaConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({AbstractTramCommandTestConfiguration.class, TramJdbcKafkaConfiguration.class, })
public class JdbcKafkaTramCommandTestConfiguration {
}
