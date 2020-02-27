package io.eventuate.tram.spring.messaging.producer.common;

import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.messaging.producer.common.MessageProducerImpl;
import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TramMessagingCommonProducerConfiguration {

  @Autowired(required=false)
  private MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @Bean
  public MessageProducer messageProducer(ChannelMapping channelMapping, MessageProducerImplementation implementation) {
    return new MessageProducerImpl(messageInterceptors, channelMapping, implementation);
  }
}
