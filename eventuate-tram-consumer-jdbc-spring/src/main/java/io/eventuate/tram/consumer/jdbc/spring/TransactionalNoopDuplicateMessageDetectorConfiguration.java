package io.eventuate.tram.consumer.jdbc.spring;

import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.consumer.jdbc.TransactionalNoopDuplicateMessageDetector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class TransactionalNoopDuplicateMessageDetectorConfiguration {

  @Bean
  public DuplicateMessageDetector duplicateMessageDetector(TransactionTemplate transactionTemplate) {
    return new TransactionalNoopDuplicateMessageDetector(transactionTemplate);
  }
}
