package io.eventuate.tram.inmemory;

import io.eventuate.tram.consumer.jdbc.TransactionalNoopDuplicateMessageDetectorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramInMemoryCommonConfiguration.class, TransactionalNoopDuplicateMessageDetectorConfiguration.class, })
public class TramInMemoryConfiguration {
}
