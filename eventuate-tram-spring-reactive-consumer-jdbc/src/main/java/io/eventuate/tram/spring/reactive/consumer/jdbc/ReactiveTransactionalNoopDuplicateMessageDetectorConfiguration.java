package io.eventuate.tram.spring.reactive.consumer.jdbc;

import io.eventuate.tram.consumer.common.reactive.ReactiveDuplicateMessageDetector;
import io.eventuate.tram.reactive.consumer.jdbc.ReactiveTransactionalNoopDuplicateMessageDetector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class ReactiveTransactionalNoopDuplicateMessageDetectorConfiguration {

  @Bean
  public ReactiveDuplicateMessageDetector duplicateMessageDetector(TransactionalOperator transactionalOperator) {
    return new ReactiveTransactionalNoopDuplicateMessageDetector(transactionalOperator);
  }
}
