package io.eventuate.tram.spring.testing.outbox.commands;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CommandOutboxTestSupportConfiguration {
  @Bean
  public CommandOutboxTestSupport commandOutboxTestSupport(JdbcTemplate jdbcTemplate) {
    return new CommandOutboxTestSupport(jdbcTemplate);
  }

}
