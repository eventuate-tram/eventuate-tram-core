package io.eventuate.tram.consumer.kafka;

import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EventuateKafkaConfigurationProperties.class)
public class TramConsumerKafkaConfiguration {

  @Bean
  public MessageConsumerKafkaImpl messageConsumerKafka(EventuateKafkaConfigurationProperties props) {
    return new MessageConsumerKafkaImpl(props.getBootstrapServers());
  }

  @Bean
  @ConditionalOnMissingBean(DuplicateMessageDetector.class)
  public DuplicateMessageDetector duplicateMessageDetector() {
    return new SqlTableBasedDuplicateMessageDetector();
  }
}
