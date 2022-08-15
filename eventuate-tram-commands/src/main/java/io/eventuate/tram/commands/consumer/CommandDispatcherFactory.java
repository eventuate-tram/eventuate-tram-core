package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;

public class CommandDispatcherFactory {

  private final MessageConsumer messageConsumer;
  private final MessageProducer messageProducer;
  private final CommandNameMapping commandNameMapping;
  private CommandReplyProducer commandReplyProducer;

  public CommandDispatcherFactory(MessageConsumer messageConsumer,
                                  MessageProducer messageProducer, CommandNameMapping commandNameMapping, CommandReplyProducer commandReplyProducer) {
    this.messageConsumer = messageConsumer;
    this.messageProducer = messageProducer;
    this.commandNameMapping = commandNameMapping;
    this.commandReplyProducer = commandReplyProducer;
  }

  public CommandDispatcher make(String commandDispatcherId,
                                CommandHandlers commandHandlers) {
    return new CommandDispatcher(commandDispatcherId, commandHandlers, messageConsumer, commandNameMapping, commandReplyProducer);
  }
}
