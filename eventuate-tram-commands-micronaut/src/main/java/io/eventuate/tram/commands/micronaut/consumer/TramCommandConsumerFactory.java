package io.eventuate.tram.commands.micronaut.consumer;

import io.eventuate.tram.commands.consumer.CommandDispatcherFactory;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class TramCommandConsumerFactory {
  @Singleton
  public CommandDispatcherFactory commandDispatcherFactory(MessageConsumer messageConsumer, MessageProducer messageProducer) {
    return new CommandDispatcherFactory(messageConsumer, messageProducer);
  }
}
