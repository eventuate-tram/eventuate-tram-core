package io.eventuate.tram.e2e.tests.redis.messages;

import io.eventuate.jdbcactivemq.TramJdbcRedisConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramJdbcRedisConfiguration.class})
public class JdbcRedisTramMessageTestConfiguration {
}
