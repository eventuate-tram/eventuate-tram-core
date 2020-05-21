package io.eventuate.tram.spring.jdbckafka;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import io.eventuate.messaging.kafka.spring.consumer.KafkaConsumerFactoryConfiguration;
import io.eventuate.tram.spring.consumer.kafka.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;

@Configuration
@Import({TramMessageProducerJdbcConfiguration.class,
        KafkaConsumerFactoryConfiguration.class,
        EventuateTramKafkaMessageConsumerConfiguration.class})
public class TramJdbcKafkaConfiguration {
}
