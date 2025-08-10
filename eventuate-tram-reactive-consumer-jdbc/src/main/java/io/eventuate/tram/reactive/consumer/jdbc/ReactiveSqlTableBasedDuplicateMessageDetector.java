package io.eventuate.tram.reactive.consumer.jdbc;

import io.eventuate.common.jdbc.EventuateDuplicateKeyException;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.spring.jdbc.reactive.EventuateSpringReactiveJdbcStatementExecutor;
import io.eventuate.tram.consumer.common.reactive.ReactiveDuplicateMessageDetector;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

public class ReactiveSqlTableBasedDuplicateMessageDetector implements ReactiveDuplicateMessageDetector {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateSchema eventuateSchema;
  private String currentTimeInMillisecondsSql;
  private TransactionalOperator transactionalOperator;
  private EventuateSpringReactiveJdbcStatementExecutor jdbcStatementExecutor;

  public ReactiveSqlTableBasedDuplicateMessageDetector(EventuateSchema eventuateSchema,
                                                       String currentTimeInMillisecondsSql,
                                                       TransactionalOperator transactionalOperator,
                                                       EventuateSpringReactiveJdbcStatementExecutor jdbcStatementExecutor) {
    this.eventuateSchema = eventuateSchema;
    this.currentTimeInMillisecondsSql = currentTimeInMillisecondsSql;
    this.transactionalOperator = transactionalOperator;
    this.jdbcStatementExecutor = jdbcStatementExecutor;
  }

  @Override
  public Mono<Boolean> isDuplicate(SubscriberIdAndMessage subscriberIdAndMessage) {
    String table = eventuateSchema.qualifyTable("received_messages");

    return jdbcStatementExecutor
        .update("insert into %s(consumer_id, message_id, creation_time) values(?, ?, %s)".formatted(table, currentTimeInMillisecondsSql),
            subscriberIdAndMessage.getSubscriberId(),
            subscriberIdAndMessage.getMessage().getId())
            .then(Mono.just(false))
            .onErrorResume(EventuateDuplicateKeyException.class, throwable -> Mono.just(true));
  }

  @Override
  public Publisher<?> doWithMessage(SubscriberIdAndMessage subscriberIdAndMessage, Publisher<?> processingFlow) {
    return Mono.defer(() -> isDuplicate(subscriberIdAndMessage)
            .flatMap(dup -> dup ? Mono.empty() : Mono.from(processingFlow))).as(transactionalOperator::transactional);

  }
}
