package io.eventuate.tram.consumer.kafka;

import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.jdbc.CommonJdbcMessagingConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonJdbcMessagingConfiguration.class, TramConsumerCommonConfiguration.class})
@EnableConfigurationProperties(EventuateKafkaConsumerConfigurationProperties.class)
public class TramConsumerKafkaConfiguration {
  @Bean
  public EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties() {
    return new EventuateKafkaConfigurationProperties();
  }

  @Bean
  public MessageConsumer messageConsumer(EventuateKafkaConfigurationProperties props) {
    return new MessageConsumerKafkaImpl(props.getBootstrapServers());
  }
}
