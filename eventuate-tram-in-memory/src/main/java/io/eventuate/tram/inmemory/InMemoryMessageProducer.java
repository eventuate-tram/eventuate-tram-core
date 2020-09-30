package io.eventuate.tram.inmemory;


import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.SimpleIdGenerator;

public class InMemoryMessageProducer implements MessageProducerImplementation {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final InMemoryMessageConsumer messageConsumer;

  private SimpleIdGenerator simpleIdGenerator = new SimpleIdGenerator();

  public InMemoryMessageProducer(InMemoryMessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

  @Override
  public void withContext(Runnable runnable) {
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      logger.info("Transaction active");
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
          runnable.run();
        }
      });
    } else {
      logger.info("No transaction active");
      runnable.run();
    }
  }

  @Override
  public String send(Message message) {
    messageConsumer.dispatchMessage(message);

    return simpleIdGenerator.generateId().toString();
  }
}
