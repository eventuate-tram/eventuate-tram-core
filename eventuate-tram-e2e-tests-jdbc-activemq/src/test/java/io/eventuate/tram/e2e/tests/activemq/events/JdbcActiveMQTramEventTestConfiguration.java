package io.eventuate.tram.e2e.tests.activemq.events;

import io.eventuate.jdbcactivemq.TramJdbcActiveMQConfiguration;
import io.eventuate.e2e.tests.basic.events.AbstractTramEventTestConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({AbstractTramEventTestConfiguration.class, TramJdbcActiveMQConfiguration.class})
public class JdbcActiveMQTramEventTestConfiguration {
}
