package io.eventuate.tram.inmemory;


import io.eventuate.common.id.IdGenerator;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class InMemoryMessageProducer implements MessageProducerImplementation {

  private final InMemoryMessageConsumer messageConsumer;
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private IdGenerator idGenerator;

  protected InMemoryMessageProducer(InMemoryMessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

  @Override
  public String generateMessageId() {
    return idGenerator.genId().asString();
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
  public void send(Message message) {
    messageConsumer.dispatchMessage(message);
  }

}
