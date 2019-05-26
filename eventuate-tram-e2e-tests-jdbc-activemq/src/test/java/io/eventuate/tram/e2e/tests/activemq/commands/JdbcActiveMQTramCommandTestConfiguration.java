package io.eventuate.tram.e2e.tests.activemq.commands;

import io.eventuate.e2e.tests.basic.commands.AbstractTramCommandTestConfiguration;
import io.eventuate.jdbcactivemq.TramJdbcActiveMQConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({AbstractTramCommandTestConfiguration.class, TramJdbcActiveMQConfiguration.class, })
public class JdbcActiveMQTramCommandTestConfiguration {
}
