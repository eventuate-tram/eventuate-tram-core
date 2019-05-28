package io.eventuate.jdbcredis;

import io.eventuate.messaging.redis.consumer.MessageConsumerRedisImpl;
import io.eventuate.messaging.redis.consumer.TramConsumerRedisConfiguration;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.wrappers.EventuateRedisMessageConsumerWrapper;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerRedisConfiguration.class,
        TramMessageProducerJdbcConfiguration.class,
        TramConsumerCommonConfiguration.class})
public class TramJdbcRedisConfiguration {
  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(MessageConsumerRedisImpl messageConsumerRedis) {
    return new EventuateRedisMessageConsumerWrapper(messageConsumerRedis);
  }
}
