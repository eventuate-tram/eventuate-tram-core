package io.eventuate.tram.mysqlkafka.integrationtests;

import io.eventuate.jdbckafka.TramJdbcKafkaConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import(TramJdbcKafkaConfiguration.class)
public class TramIntegrationTestConfiguration {
}
