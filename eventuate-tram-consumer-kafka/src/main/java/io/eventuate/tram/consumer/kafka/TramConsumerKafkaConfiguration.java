package io.eventuate.tram.consumer.kafka;

import io.eventuate.common.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.common.kafka.EventuateKafkaPropertiesConfiguration;
import io.eventuate.common.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.EventuateKafkaPropertiesConfiguration;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.TramConsumerCommonConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramConsumerCommonConfiguration.class,
        EventuateKafkaPropertiesConfiguration.class})
@EnableConfigurationProperties(EventuateKafkaConsumerConfigurationProperties.class)
public class TramConsumerKafkaConfiguration {

  @Bean
  public MessageConsumerImplementation messageConsumerImplementation(EventuateKafkaConfigurationProperties props) {
    return new MessageConsumerKafkaImpl(props.getBootstrapServers());
  }
}
