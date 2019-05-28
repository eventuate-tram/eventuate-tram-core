package io.eventuate.jdbcrabbitmq;

import io.eventuate.tram.consumer.rabbitmq.TramConsumerRabbitMQConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerRabbitMQConfiguration.class, TramMessageProducerJdbcConfiguration.class})
public class TramJdbcRabbitMQConfiguration {
}
