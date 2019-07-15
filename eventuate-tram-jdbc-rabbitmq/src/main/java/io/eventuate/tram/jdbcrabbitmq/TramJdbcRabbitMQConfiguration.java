package io.eventuate.tram.jdbcrabbitmq;

import io.eventuate.tram.consumer.rabbitmq.EventuateTramRabbitMQMessageConsumerConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramMessageProducerJdbcConfiguration.class,
        EventuateTramRabbitMQMessageConsumerConfiguration.class})
public class TramJdbcRabbitMQConfiguration {
}
