package io.eventuate.tram.reactive.integrationtests;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.spring.jdbc.reactive.EventuateSpringReactiveJdbcStatementExecutor;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import io.eventuate.tram.reactive.consumer.jdbc.ReactiveSqlTableBasedDuplicateMessageDetector;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.eventuate.util.test.async.Eventually.eventually;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReactiveTramMessagingDuplicateDetectionTest {
  private String subscriberId = "subscriberId";
  private String messageId = "messageId";
  private String payload = "payload";

  private String insertIntoReceivedMessageSql =
          "insert into eventuate.received_messages(consumer_id, message_id, creation_time) values(?, ?, )";

  @Test
  public void shouldInvokeTransactionalForDuplicate() {
    Supplier<Mono<Void>> messageHandlerInvocationFlag = mockMessageHandler();
    EventuateSpringReactiveJdbcStatementExecutor jdbcStatementExecutor = mockReactiveJdbcStatementExecutor();
    ReactiveTransactionManager transactionManager = mockTransactionManager();
    TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);

    ReactiveSqlTableBasedDuplicateMessageDetector duplicateMessageDetector =
            new ReactiveSqlTableBasedDuplicateMessageDetector(new EventuateSchema(EventuateSchema.DEFAULT_SCHEMA),
                    "", transactionalOperator, jdbcStatementExecutor);

    duplicateMessageDetector.doWithMessage(new SubscriberIdAndMessage(subscriberId, new MessageImpl(payload, Collections.singletonMap("ID", messageId))),
            Mono.defer(messageHandlerInvocationFlag)).block();

    InOrder verificationOrder = inOrder(transactionManager, jdbcStatementExecutor, messageHandlerInvocationFlag);

    eventually(10, 500, TimeUnit.MILLISECONDS, () -> {
      verificationOrder.verify(transactionManager).getReactiveTransaction(any());

      verificationOrder.verify(jdbcStatementExecutor).update(insertIntoReceivedMessageSql, subscriberId, messageId);

      verificationOrder.verify(messageHandlerInvocationFlag).get();

      verificationOrder.verify(transactionManager).commit(any());
    });
  }

  private Supplier<Mono<Void>> mockMessageHandler() {
    Supplier<Mono<Void>> messageHandler = mock(Supplier.class);
    when(messageHandler.get()).thenReturn(Mono.empty().then());
    return messageHandler;
  }

  private EventuateSpringReactiveJdbcStatementExecutor mockReactiveJdbcStatementExecutor() {
    EventuateSpringReactiveJdbcStatementExecutor jdbcStatementExecutor = mock(EventuateSpringReactiveJdbcStatementExecutor.class);

    when(jdbcStatementExecutor.update(anyString(), any())).thenReturn(Mono.defer(() -> Mono.just(1)));

    return jdbcStatementExecutor;
  }

  private ReactiveTransactionManager mockTransactionManager() {
    ReactiveTransactionManager transactionManager = mock(ReactiveTransactionManager.class);

    when(transactionManager.getReactiveTransaction(any())).thenReturn(Mono.defer(() -> Mono.just(mockReactiveTransaction())));
    when(transactionManager.commit(any())).thenReturn(Mono.empty());

    return transactionManager;
  }

  private ReactiveTransaction mockReactiveTransaction() {
    ReactiveTransaction reactiveTransaction = mock(ReactiveTransaction.class);

    when(reactiveTransaction.isNewTransaction()).thenReturn(false);
    when(reactiveTransaction.isRollbackOnly()).thenReturn(false);
    when(reactiveTransaction.isCompleted()).thenReturn(false);

    return reactiveTransaction;
  }
}
