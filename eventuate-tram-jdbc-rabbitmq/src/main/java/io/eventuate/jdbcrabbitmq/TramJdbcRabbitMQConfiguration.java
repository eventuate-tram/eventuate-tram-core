package io.eventuate.jdbcrabbitmq;

import io.eventuate.tram.consumer.jdbc.TramConsumerJdbcConfiguration;
import io.eventuate.tram.consumer.rabbitmq.TramConsumerRabbitMQConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerRabbitMQConfiguration.class, TramMessageProducerJdbcConfiguration.class, TramConsumerJdbcConfiguration.class})
public class TramJdbcRabbitMQConfiguration {
}
