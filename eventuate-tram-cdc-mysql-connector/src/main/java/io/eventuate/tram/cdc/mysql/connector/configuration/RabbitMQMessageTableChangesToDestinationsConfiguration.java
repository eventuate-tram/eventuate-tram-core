package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.tram.data.producer.rabbitmq.EventuateRabbitMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RabbitMQMessageTableChangesToDestinationsConfiguration {
  @Bean
  @Profile("RabbitMQ")
  public DataProducerFactory rabbitMQDataProducerFactory(@Value("${rabbitmq.url}") String rabbitMQURL) {
    return () -> new EventuateRabbitMQProducer(rabbitMQURL);
  }
}
