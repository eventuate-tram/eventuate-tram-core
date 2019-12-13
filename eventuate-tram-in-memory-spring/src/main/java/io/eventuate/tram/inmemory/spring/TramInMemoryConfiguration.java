package io.eventuate.tram.inmemory.spring;

import io.eventuate.tram.consumer.jdbc.spring.TransactionalNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.inmemory.TramInMemoryCommonConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramInMemoryCommonConfiguration.class, TransactionalNoopDuplicateMessageDetectorConfiguration.class, })
public class TramInMemoryConfiguration {
}
