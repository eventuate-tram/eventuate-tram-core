package io.eventuate.tram.consumer.common;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.tram.jdbc.CommonJdbcMessagingConfiguration;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.common.sql.SqlDialectConfiguration;
import io.eventuate.tram.messaging.common.sql.SqlDialectSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
@Import({SqlDialectConfiguration.class, CommonJdbcMessagingConfiguration.class})
public class TramConsumerCommonConfiguration {
  @Bean
  @ConditionalOnMissingBean(DuplicateMessageDetector.class)
  public DuplicateMessageDetector duplicateMessageDetector(EventuateSchema eventuateSchema,
                                                           SqlDialectSelector sqlDialectSelector, TransactionTemplate transactionTemplate) {
    return new SqlTableBasedDuplicateMessageDetector(eventuateSchema,
            sqlDialectSelector.getDialect().getCurrentTimeInMillisecondsExpression(), transactionTemplate);
  }

  @Autowired(required=false)
  private MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @Bean
  public DecoratedMessageHandlerFactory subscribedMessageHandlerChainFactory(List<MessageHandlerDecorator> decorators) {
    return new DecoratedMessageHandlerFactory(decorators);
  }

  @Bean
  public PrePostReceiveMessageHandlerDecorator prePostReceiveMessageHandlerDecoratorDecorator() {
    return new PrePostReceiveMessageHandlerDecorator(messageInterceptors);
  }

  @Bean
  public DuplicateDetectingMessageHandlerDecorator duplicateDetectingMessageHandlerDecorator(DuplicateMessageDetector duplicateMessageDetector) {
    return new DuplicateDetectingMessageHandlerDecorator(duplicateMessageDetector);
  }

  @Bean
  public PrePostHandlerMessageHandlerDecorator prePostHandlerMessageHandlerDecorator() {
    return new PrePostHandlerMessageHandlerDecorator(messageInterceptors);
  }
}
