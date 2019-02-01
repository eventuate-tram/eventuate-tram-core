package io.eventuate.tram.messaging.common.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
  public SqlDialectSelector sqlDialectSelector(@Value("${spring.datasource.driver.class.name}") String driver) {
    return new SqlDialectSelector(driver);
  }
}
