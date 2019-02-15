package io.eventuate.jdbcactivemq;

import io.eventuate.tram.consumer.redis.TramConsumerRedisConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerRedisConfiguration.class, TramMessageProducerJdbcConfiguration.class})
public class TramJdbcRedisConfiguration {

}
