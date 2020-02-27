package io.eventuate.tram.micronaut.messaging.producer.common;

import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.messaging.producer.common.MessageProducerImpl;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class TramMessagingCommonProducerFactory {
  @Singleton
  public MessageProducer messageProducer(MessageInterceptor[] messageInterceptors,
                                         ChannelMapping channelMapping,
                                         MessageProducerImplementation implementation) {
    return new MessageProducerImpl(messageInterceptors, channelMapping, implementation);
  }
}
