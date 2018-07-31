package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.local.db.log.common.DuplicatePublishingDetector;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducerConfigurationProperties;
import io.eventuate.tram.cdc.mysql.connector.EventuateTramChannelProperties;
import io.eventuate.tram.cdc.mysql.connector.configuration.condition.KafkaCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({EventuateKafkaProducerConfigurationProperties.class,
        EventuateKafkaConsumerConfigurationProperties.class})
public class KafkaMessageTableChangesToDestinationsConfiguration {
  @Bean
  @Conditional(KafkaCondition.class)
  public PublishingFilter kafkaDuplicatePublishingDetector(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                           EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    return new DuplicatePublishingDetector(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            eventuateKafkaConsumerConfigurationProperties);
  }

  @Bean
  @Conditional(KafkaCondition.class)
  public DataProducerFactory kafkaDataProducerFactory(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                      EventuateKafkaProducerConfigurationProperties eventuateKafkaProducerConfigurationProperties) {
    return () -> new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers(),
            eventuateKafkaProducerConfigurationProperties);
  }
}
