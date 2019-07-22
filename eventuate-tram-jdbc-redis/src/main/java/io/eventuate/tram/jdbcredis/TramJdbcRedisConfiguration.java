package io.eventuate.tram.jdbcredis;

import io.eventuate.tram.consumer.redis.EventuateTramRedisMessageConsumerConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.spring.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramMessageProducerJdbcConfiguration.class, EventuateTramRedisMessageConsumerConfiguration.class})
public class TramJdbcRedisConfiguration {
}
