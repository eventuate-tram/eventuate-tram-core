package io.eventuate.tram.e2e.tests.kafka.events;

import io.eventuate.jdbckafka.TramJdbcKafkaConfiguration;
import io.eventuate.e2e.tests.basic.events.AbstractTramEventTestConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({AbstractTramEventTestConfiguration.class, TramJdbcKafkaConfiguration.class})
public class JdbcKafkaTramEventTestConfiguration {
}
