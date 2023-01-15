package io.eventuate.tram.reactive.commands.consumer;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;

public class ReactiveCommandDispatcherFactory {

  private final ReactiveMessageConsumer messageConsumer;
  private final ReactiveCommandReplyProducer commandReplyProducer;

  public ReactiveCommandDispatcherFactory(ReactiveMessageConsumer messageConsumer,
                                          ReactiveCommandReplyProducer commandReplyProducer) {
    this.messageConsumer = messageConsumer;
    this.commandReplyProducer = commandReplyProducer;
  }

  public ReactiveCommandDispatcher make(String commandDispatcherId,
                                        ReactiveCommandHandlers commandHandlers) {
    ReactiveCommandDispatcher reactiveCommandDispatcher = new ReactiveCommandDispatcher(commandDispatcherId, commandHandlers, messageConsumer, commandReplyProducer);
    reactiveCommandDispatcher.initialize();
    return reactiveCommandDispatcher;
  }
}
