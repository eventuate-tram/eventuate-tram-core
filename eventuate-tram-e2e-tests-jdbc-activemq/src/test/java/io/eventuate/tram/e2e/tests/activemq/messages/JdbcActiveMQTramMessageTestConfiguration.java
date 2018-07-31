package io.eventuate.tram.e2e.tests.activemq.messages;

import io.eventuate.jdbcactivemq.TramJdbcActiveMQConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramJdbcActiveMQConfiguration.class})
public class JdbcActiveMQTramMessageTestConfiguration {
}
