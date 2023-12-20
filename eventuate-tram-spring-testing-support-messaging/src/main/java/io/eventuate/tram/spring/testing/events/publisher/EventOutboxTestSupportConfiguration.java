package io.eventuate.tram.spring.testing.events.publisher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class EventOutboxTestSupportConfiguration {

    @Bean
    public EventOutboxTestSupport eventOutboxTestSupport(JdbcTemplate jdbcTemplate) {
        return new EventOutboxTestSupport(jdbcTemplate);
    }

}
