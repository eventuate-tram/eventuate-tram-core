package io.eventuate.tram.integrationtest.common;

import io.eventuate.tram.jdbcredis.TramJdbcRedisConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(TramJdbcRedisConfiguration.class)
@Conditional(MySqlBinlogRedisCondition.class)
public class TramIntegrationTestMySqlBinlogRedisConfiguration {
}
