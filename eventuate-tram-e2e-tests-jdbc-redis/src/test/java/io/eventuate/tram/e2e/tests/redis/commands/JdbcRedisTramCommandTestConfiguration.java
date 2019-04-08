package io.eventuate.tram.e2e.tests.redis.commands;

import io.eventuate.e2e.tests.basic.commands.AbstractTramCommandTestConfiguration;
import io.eventuate.jdbcredis.TramJdbcRedisConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({AbstractTramCommandTestConfiguration.class, TramJdbcRedisConfiguration.class})
public class JdbcRedisTramCommandTestConfiguration {
}
