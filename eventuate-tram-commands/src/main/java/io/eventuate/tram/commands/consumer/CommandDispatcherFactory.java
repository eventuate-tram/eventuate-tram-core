package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;

public class CommandDispatcherFactory {

  private final MessageConsumer messageConsumer;
  private final MessageProducer messageProducer;
  private CommandNameMapping commandNameMapping;

  public CommandDispatcherFactory(MessageConsumer messageConsumer,
                                  MessageProducer messageProducer, CommandNameMapping commandNameMapping) {
    this.messageConsumer = messageConsumer;
    this.messageProducer = messageProducer;
    this.commandNameMapping = commandNameMapping;
  }

  public CommandDispatcher make(String commandDispatcherId,
                                CommandHandlers commandHandlers) {
    return new CommandDispatcher(commandDispatcherId, commandHandlers, messageConsumer, messageProducer, commandNameMapping);
  }
}
