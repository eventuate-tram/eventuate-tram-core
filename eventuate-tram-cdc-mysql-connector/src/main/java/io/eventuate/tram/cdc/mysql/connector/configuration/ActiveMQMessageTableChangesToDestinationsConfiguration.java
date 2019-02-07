package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.tram.data.producer.activemq.EventuateActiveMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ActiveMQMessageTableChangesToDestinationsConfiguration {
  @Bean
  @Profile("ActiveMQ")
  public DataProducerFactory activeMQDataProducerFactory(@Value("${activemq.url}") String activeMQURL) {
    return () -> new EventuateActiveMQProducer(activeMQURL);
  }
}
