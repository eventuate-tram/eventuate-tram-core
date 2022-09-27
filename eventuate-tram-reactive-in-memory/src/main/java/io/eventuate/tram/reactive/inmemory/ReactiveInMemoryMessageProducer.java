package io.eventuate.tram.reactive.inmemory;


import io.eventuate.common.id.ApplicationIdGenerator;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducerImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class ReactiveInMemoryMessageProducer implements ReactiveMessageProducerImplementation {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ReactiveInMemoryMessageConsumer messageConsumer;

  private final ApplicationIdGenerator applicationIdGenerator = new ApplicationIdGenerator();

  public ReactiveInMemoryMessageProducer(ReactiveInMemoryMessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

//  @Override
//  public void withContext(Runnable runnable) {
//    if (eventuateTransactionSynchronizationManager.isTransactionActive()) {
//      logger.info("Transaction active");
//      eventuateTransactionSynchronizationManager.executeAfterTransaction(runnable);
//    } else {
//      logger.info("No transaction active");
//      runnable.run();
//    }
//  }
//
//  @Override
//  public void setMessageIdIfNecessary(Message message) {
//    message.setHeader(Message.ID, applicationIdGenerator.genId(null).asString());
//  }
//
  @Override
  public Mono<Message> send(Message message) {
    if (!message.getHeader(Message.ID).isPresent())
      message.setHeader(Message.ID, applicationIdGenerator.genId(null, null).asString());
    messageConsumer.dispatchMessage(message);
    return Mono.just(message);
  }
}
