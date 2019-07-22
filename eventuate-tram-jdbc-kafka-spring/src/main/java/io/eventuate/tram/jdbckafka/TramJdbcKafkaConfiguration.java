package io.eventuate.tram.jdbckafka;

import io.eventuate.tram.consumer.kafka.spring.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.spring.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramMessageProducerJdbcConfiguration.class,
        EventuateTramKafkaMessageConsumerConfiguration.class})
public class TramJdbcKafkaConfiguration {
}
