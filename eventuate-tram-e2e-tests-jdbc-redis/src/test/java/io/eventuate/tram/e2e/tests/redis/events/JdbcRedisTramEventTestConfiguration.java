package io.eventuate.tram.e2e.tests.redis.events;

import io.eventuate.e2e.tests.basic.events.AbstractTramEventTestConfiguration;
import io.eventuate.jdbcredis.TramJdbcRedisConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({AbstractTramEventTestConfiguration.class, TramJdbcRedisConfiguration.class})
public class JdbcRedisTramEventTestConfiguration {
}
