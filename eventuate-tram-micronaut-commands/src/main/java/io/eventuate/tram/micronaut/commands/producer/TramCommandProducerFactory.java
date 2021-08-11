package io.eventuate.tram.micronaut.commands.producer;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class TramCommandProducerFactory {
  @Singleton
  public CommandProducer commandProducer(MessageProducer messageProducer, ChannelMapping channelMapping, CommandNameMapping commandNameMapping) {
    return new CommandProducerImpl(messageProducer, commandNameMapping);
  }
}
