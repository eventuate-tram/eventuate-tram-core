package io.eventuate.tram.broker.db.integrationtests;

import io.eventuate.tram.integrationtest.common.TramIntegrationTestMySqlBinlogKafkaConfiguration;
import io.eventuate.tram.integrationtest.common.TramIntegrationTestMySqlBinlogRedisConfiguration;
import io.eventuate.tram.integrationtest.common.TramIntegrationTestPollingActiveMQConfiguration;
import io.eventuate.tram.integrationtest.common.TramIntegrationTestPostgresWalRabbitMQConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramIntegrationTestMySqlBinlogKafkaConfiguration.class,
        TramIntegrationTestPostgresWalRabbitMQConfiguration.class,
        TramIntegrationTestPollingActiveMQConfiguration.class,
        TramIntegrationTestMySqlBinlogRedisConfiguration.class})
public class TramIntegrationTestConfiguration {
}
