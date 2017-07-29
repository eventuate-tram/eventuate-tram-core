package io.eventuate.jdbckafka;

import io.eventuate.tram.consumer.kafka.TramConsumerKafkaConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerKafkaConfiguration.class, TramMessageProducerJdbcConfiguration.class, })
public class TramJdbcKafkaConfiguration {
}
