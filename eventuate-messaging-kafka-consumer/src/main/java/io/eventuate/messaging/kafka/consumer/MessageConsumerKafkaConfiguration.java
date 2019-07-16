package io.eventuate.messaging.kafka.consumer;

import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.EventuateKafkaPropertiesConfiguration;
import io.eventuate.messaging.kafka.basic.consumer.EventuateKafkaConsumerConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({EventuateKafkaPropertiesConfiguration.class})
@EnableConfigurationProperties(EventuateKafkaConsumerConfigurationProperties.class)
public class MessageConsumerKafkaConfiguration {
  @Bean
  public EventuateKafkaConsumer messageConsumerKafka(EventuateKafkaConfigurationProperties props,
                                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    return new EventuateKafkaConsumer(props.getBootstrapServers(), eventuateKafkaConsumerConfigurationProperties);
  }
}
