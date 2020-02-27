package io.eventuate.tram.jdbcactivemq;

import io.eventuate.tram.consumer.activemq.EventuateTramActiveMQMessageConsumerConfiguration;
import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramMessageProducerJdbcConfiguration.class, EventuateTramActiveMQMessageConsumerConfiguration.class})
public class TramJdbcActiveMQConfiguration {
}
