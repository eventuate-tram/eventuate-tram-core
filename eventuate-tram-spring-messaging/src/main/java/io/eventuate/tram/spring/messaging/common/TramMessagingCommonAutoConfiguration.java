package io.eventuate.tram.spring.messaging.common;

import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.consumer.DefaultSubscriberMapping;
import io.eventuate.tram.messaging.consumer.SubscriberMapping;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class TramMessagingCommonAutoConfiguration {

  @ConditionalOnMissingBean(ChannelMapping.class)
  @Bean
  public ChannelMapping channelMapping() {
    return new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
  }

  @ConditionalOnMissingBean(SubscriberMapping.class)
  @Bean
  public SubscriberMapping subscriberMapping() {
    return new DefaultSubscriberMapping();
  }
}
