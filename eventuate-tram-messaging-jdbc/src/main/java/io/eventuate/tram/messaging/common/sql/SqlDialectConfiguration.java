package io.eventuate.tram.messaging.common.sql;

import io.eventuate.tram.jdbc.CommonJdbcMessagingConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonJdbcMessagingConfiguration.class)
public class SqlDialectConfiguration {

  @Bean
  public MySqlDialect mySqlDialect() {
    return new MySqlDialect();
  }

  @Bean
  public PostgreSqlDialect postgreSQLDialect() {
    return new PostgreSqlDialect();
  }

  @Bean
  public DefaultSqlDialect defaultSqlDialect() {
    return new DefaultSqlDialect();
  }

  @Bean
  public SqlDialectSelector sqlDialectSelector(@Value("${spring.datasource.driver-class-name}") String driver) {
    return new SqlDialectSelector(driver);
  }
}
