package io.eventuate.tram.consumer.kafka;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.tram.messaging.common.CommonMessagingConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(EventuateKafkaConsumerConfigurationProperties.class)
@Import(CommonMessagingConfiguration.class)
public class TramConsumerKafkaConfiguration {

  @Bean
  public EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties() {
    return new EventuateKafkaConfigurationProperties();
  }

  @Bean
  public MessageConsumerKafkaImpl messageConsumerKafka(EventuateKafkaConfigurationProperties props) {
    return new MessageConsumerKafkaImpl(props.getBootstrapServers());
  }

  @Bean
  @ConditionalOnMissingBean(DuplicateMessageDetector.class)
  public DuplicateMessageDetector duplicateMessageDetector(EventuateSchema eventuateSchema) {
    return new SqlTableBasedDuplicateMessageDetector(eventuateSchema);
  }
}
