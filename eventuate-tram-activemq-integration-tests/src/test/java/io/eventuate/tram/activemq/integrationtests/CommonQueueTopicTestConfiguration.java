package io.eventuate.tram.activemq.integrationtests;

import io.eventuate.common.ChannelType;
import io.eventuate.tram.cdc.connector.EventuateTramChannelProperties;
import io.eventuate.tram.consumer.activemq.MessageConsumerActiveMQImpl;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.consumer.common.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.data.producer.activemq.EventuateActiveMQProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(EventuateTramChannelProperties.class)
@EnableAutoConfiguration
@Import({TramConsumerCommonConfiguration.class, TramNoopDuplicateMessageDetectorConfiguration.class})
public class CommonQueueTopicTestConfiguration {

  @Bean
  @Qualifier("uniquePostfix")
  public String uniquePostfix() {
    return UUID.randomUUID().toString();
  }

  @Bean
  public MessageConsumerActiveMQImpl messageConsumerKafka(@Value("${activemq.url}") String activeMQURL,
                                                          @Qualifier("uniquePostfix") String uniquePostfix,
                                                          @Qualifier("testChannelType") ChannelType channelType) {
    return new MessageConsumerActiveMQImpl(activeMQURL,
            Collections.singletonMap("destination" + uniquePostfix, channelType));
  }

  @Bean
  public EventuateActiveMQProducer activeMQMessageProducer(@Value("${activemq.url}") String activeMQURL,
                                                           @Qualifier("uniquePostfix") String uniquePostfix,
                                                           @Qualifier("testChannelType") ChannelType channelType) {
    return new EventuateActiveMQProducer(activeMQURL,
            Collections.singletonMap("destination" + uniquePostfix, channelType));
  }
}
