package io.eventuate.tram.micronaut.commands.consumer;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.micronaut.context.annotation.Factory;

import jakarta.inject.Singleton;

@Factory
public class TramCommandConsumerFactory {
  @Singleton
  public CommandDispatcherFactory commandDispatcherFactory(MessageConsumer messageConsumer, CommandNameMapping commandNameMapping, CommandReplyProducer commandReplyProducer) {
    return new CommandDispatcherFactory(messageConsumer, commandNameMapping, commandReplyProducer);
  }

  @Singleton
  public CommandReplyProducer commandReplyProducer(MessageProducer messageProducer) {
    return new CommandReplyProducer(messageProducer);
  }
}
