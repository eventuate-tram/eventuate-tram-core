package io.eventuate.tram.e2e.tests.redis.messages;

import io.eventuate.jdbcredis.TramJdbcRedisConfiguration;
import io.eventuate.tram.consumer.common.NoopDuplicateMessageDetector;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramJdbcRedisConfiguration.class, NoopDuplicateMessageDetector.class})
public class JdbcRedisTramMessageTestConfiguration {
}
