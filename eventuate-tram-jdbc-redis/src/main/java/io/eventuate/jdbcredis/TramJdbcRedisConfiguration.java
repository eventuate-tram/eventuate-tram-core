package io.eventuate.jdbcredis;

import io.eventuate.tram.consumer.jdbc.TramConsumerJdbcConfiguration;
import io.eventuate.tram.consumer.redis.TramConsumerRedisConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerRedisConfiguration.class, TramMessageProducerJdbcConfiguration.class, TramConsumerJdbcConfiguration.class})
public class TramJdbcRedisConfiguration {

}
