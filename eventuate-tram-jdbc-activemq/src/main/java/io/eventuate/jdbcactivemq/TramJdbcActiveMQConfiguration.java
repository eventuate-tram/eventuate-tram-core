package io.eventuate.jdbcactivemq;

import io.eventuate.tram.consumer.activemq.TramConsumerActiveMQConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerActiveMQConfiguration.class, TramMessageProducerJdbcConfiguration.class, })
public class TramJdbcActiveMQConfiguration {
}
