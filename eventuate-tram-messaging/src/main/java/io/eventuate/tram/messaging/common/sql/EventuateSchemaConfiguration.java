package io.eventuate.tram.messaging.common.sql;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventuateSchemaConfiguration {
    @Bean
    public EventuateSchema eventuateSchema(@Value("${eventuate.database.schema:#{null}}") String eventuateDatabaseSchema) {
        return new EventuateSchema(eventuateDatabaseSchema);
    }
}
