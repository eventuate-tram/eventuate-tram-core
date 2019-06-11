package io.eventuate.tram.integrationtest.common;

import io.eventuate.jdbcactivemq.TramJdbcActiveMQConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramJdbcActiveMQConfiguration.class)
@Conditional(PollingActiveMQCondition.class)
public class TramIntegrationTestPollingActiveMQConfiguration {
}
