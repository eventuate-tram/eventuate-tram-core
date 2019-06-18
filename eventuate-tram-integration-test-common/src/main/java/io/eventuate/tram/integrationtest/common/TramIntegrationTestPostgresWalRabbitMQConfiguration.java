package io.eventuate.tram.integrationtest.common;

import io.eventuate.tram.jdbcrabbitmq.TramJdbcRabbitMQConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramJdbcRabbitMQConfiguration.class)
@Conditional(PostgresWalRabbitMQCondition.class)
public class TramIntegrationTestPostgresWalRabbitMQConfiguration {
}
