package io.eventuate.tram.spring.jdbckafka;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import io.eventuate.messaging.kafka.spring.consumer.KafkaConsumerConfigurerConfiguration;
import io.eventuate.tram.spring.consumer.kafka.EventuateTramKafkaMessageConsumerConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;

@Configuration
@Import({TramMessageProducerJdbcConfiguration.class,
        EventuateTramKafkaMessageConsumerConfiguration.class,
         KafkaConsumerConfigurerConfiguration.class})
public class TramJdbcKafkaConfiguration {
}
