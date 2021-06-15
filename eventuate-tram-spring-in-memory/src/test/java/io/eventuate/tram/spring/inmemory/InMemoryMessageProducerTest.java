package io.eventuate.tram.spring.inmemory;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.inmemory.test.AbstractInMemoryMessageProducerTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = InMemoryMessageProducerTest.InMemoryMessagingTestConfiguration.class)
public class InMemoryMessageProducerTest extends AbstractInMemoryMessageProducerTest {
  @Configuration
  @EnableAutoConfiguration
  @Import(TramInMemoryConfiguration.class)
  public static class InMemoryMessagingTestConfiguration {
  }

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private MessageProducer messageProducer;

  @Autowired
  private MessageConsumer messageConsumer;

  @Override
  @Before
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
    transactionTemplate.execute(status -> {
      callbackWithRollback.accept(status::setRollbackOnly);

      return null;
    });
  }
}