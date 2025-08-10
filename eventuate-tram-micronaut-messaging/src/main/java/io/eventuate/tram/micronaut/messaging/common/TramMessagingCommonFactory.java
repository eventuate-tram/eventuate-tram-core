package io.eventuate.tram.micronaut.messaging.common;

import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.consumer.DefaultSubscriberMapping;
import io.eventuate.tram.messaging.consumer.SubscriberMapping;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import jakarta.inject.Singleton;

@Factory
public class TramMessagingCommonFactory {

  @Singleton
  @Requires(missingBeans = ChannelMapping.class)
  public ChannelMapping channelMapping() {
    return new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
  }

  @Singleton
  @Requires(missingBeans = SubscriberMapping.class)
  public SubscriberMapping subscriberMapping() {
    return new DefaultSubscriberMapping();
  }
}
