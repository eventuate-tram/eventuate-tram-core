package io.eventuate.tram.micronaut.inmemory;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.inmemory.test.AbstractInMemoryMessageProducerTest;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.SynchronousTransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.sql.Connection;
import java.util.function.Consumer;

@MicronautTest(transactional = false)
public class InMemoryMessageProducerTest extends AbstractInMemoryMessageProducerTest {

  @Inject
  private MessageProducer messageProducer;

  @Inject
  private MessageConsumer messageConsumer;

  @Inject
  private SynchronousTransactionManager<Connection> transactionManager;

  @Override
  protected MessageProducer getMessageProducer() {
    return messageProducer;
  }

  @Override
  protected MessageConsumer getMessageConsumer() {
    return messageConsumer;
  }

  @Override
  protected void executeInTransaction(Consumer<Runnable> callbackWithRollback) {
    transactionManager.executeWrite(status -> {
      callbackWithRollback.accept(status::setRollbackOnly);
      return null;
    });
  }

  @Override
  @BeforeEach
  public void setUp() {
    super.setUp();
  }

  @Override
  @Test
  public void shouldDeliverToMatchingSubscribers() {
    super.shouldDeliverToMatchingSubscribers();
  }

  @Override
  @Test
  public void shouldSetIdWithinTransaction() {
    super.shouldSetIdWithinTransaction();
  }

  @Override
  @Test
  public void shouldDeliverToWildcardSubscribers() {
    super.shouldDeliverToWildcardSubscribers();
  }

  @Override
  @Test
  public void shouldReceiveMessageAfterTransaction() {
    super.shouldReceiveMessageAfterTransaction();
  }

  @Override
  @Test
  public void shouldNotReceiveMessageBeforeTransaction() {
    super.shouldNotReceiveMessageBeforeTransaction();
  }

  @Override
  @Test
  public void shouldNotReceiveMessageAfterTransactionRollback() {
    super.shouldNotReceiveMessageAfterTransactionRollback();
  }
}