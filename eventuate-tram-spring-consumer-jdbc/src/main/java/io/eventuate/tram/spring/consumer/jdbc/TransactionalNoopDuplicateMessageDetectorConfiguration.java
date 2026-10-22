package io.eventuate.tram.spring.consumer.jdbc;

import io.eventuate.common.database.jdbc.EventuateTransactionTemplate;
import io.eventuate.common.database.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.common.database.spring.jdbc.EventuateTransactionTemplateConfiguration;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.jdbc.TransactionalNoopDuplicateMessageDetector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Import(EventuateTransactionTemplateConfiguration.class)
public class TransactionalNoopDuplicateMessageDetectorConfiguration {

  @Bean
  public DuplicateMessageDetector duplicateMessageDetector(EventuateTransactionTemplate eventuateTransactionTemplate) {
    return new TransactionalNoopDuplicateMessageDetector(eventuateTransactionTemplate);
  }
}
