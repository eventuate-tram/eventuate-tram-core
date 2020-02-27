package io.eventuate.tram.integrationtest.common;

import io.eventuate.tram.spring.jdbckafka.TramJdbcKafkaConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramJdbcKafkaConfiguration.class)
@Conditional(MySqlBinlogKafkaCondition.class)
public class TramIntegrationTestMySqlBinlogKafkaConfiguration {
}
