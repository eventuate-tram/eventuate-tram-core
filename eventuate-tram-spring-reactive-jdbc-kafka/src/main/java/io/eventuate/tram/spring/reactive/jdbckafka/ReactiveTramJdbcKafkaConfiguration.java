package io.eventuate.tram.spring.reactive.jdbckafka;

import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import io.eventuate.tram.spring.reactive.consumer.kafka.EventuateTramReactiveKafkaMessageConsumerConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ReactiveTramMessageProducerJdbcConfiguration.class,
        EventuateTramReactiveKafkaMessageConsumerConfiguration.class})
public class ReactiveTramJdbcKafkaConfiguration {
}
