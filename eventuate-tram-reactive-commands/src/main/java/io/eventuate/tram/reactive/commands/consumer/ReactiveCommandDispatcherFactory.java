package io.eventuate.tram.reactive.commands.consumer;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;

public class ReactiveCommandDispatcherFactory {

  private final ReactiveMessageConsumer messageConsumer;
  private final ReactiveMessageProducer messageProducer;

  public ReactiveCommandDispatcherFactory(ReactiveMessageConsumer messageConsumer,
                                          ReactiveMessageProducer messageProducer) {
    this.messageConsumer = messageConsumer;
    this.messageProducer = messageProducer;
  }

  public ReactiveCommandDispatcher make(String commandDispatcherId,
                                        ReactiveCommandHandlers commandHandlers) {
    return new ReactiveCommandDispatcher(commandDispatcherId, commandHandlers, messageConsumer, messageProducer);
  }
}
