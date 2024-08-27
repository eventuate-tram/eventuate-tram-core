package io.eventuate.tram.micronaut.consumer.common;

import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.MessageConsumerImpl;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.SubscriberMapping;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class TramConsumerCommonFactory {

  @Singleton
  public MessageConsumer messageConsumer(MessageConsumerImplementation messageConsumerImplementation,
                                         ChannelMapping channelMapping,
                                         DecoratedMessageHandlerFactory decoratedMessageHandlerFactory, SubscriberMapping subscriberMapping) {
    return new MessageConsumerImpl(channelMapping, messageConsumerImplementation, decoratedMessageHandlerFactory, subscriberMapping);
  }
}
