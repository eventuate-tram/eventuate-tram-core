package io.eventuate.tram.jdbcredis;

import io.eventuate.messaging.redis.consumer.MessageConsumerRedisConfiguration;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.redis.EventuateTramRedisMessageConsumerConfiguration;
import io.eventuate.tram.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MessageConsumerRedisConfiguration.class,
        TramMessageProducerJdbcConfiguration.class,
        TramConsumerCommonConfiguration.class,
        EventuateTramRedisMessageConsumerConfiguration.class})
public class TramJdbcRedisConfiguration {
}
