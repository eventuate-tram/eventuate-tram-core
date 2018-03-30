package io.eventuate.tram.mysqlkafka.integrationtests;

import io.eventuate.jdbckafka.TramJdbcKafkaConfiguration;
import io.eventuate.tram.cdc.mysql.connector.MessageTableChangesToDestinationsConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(TramJdbcKafkaConfiguration.class)
@SpringBootApplication(exclude = MessageTableChangesToDestinationsConfiguration.class)
public class TramIntegrationTestConfiguration {
}
