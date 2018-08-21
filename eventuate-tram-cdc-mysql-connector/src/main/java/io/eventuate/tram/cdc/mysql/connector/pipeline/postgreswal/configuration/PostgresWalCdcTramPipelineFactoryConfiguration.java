package io.eventuate.tram.cdc.mysql.connector.pipeline.postgreswal.configuration;

import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.tram.cdc.mysql.connector.PostgresWalOffsetStoreFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.postgreswal.factory.PostgresWalCdcTramPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostgresWalCdcTramPipelineFactoryConfiguration {
  @Bean("eventuateTramPostgresWal")
  public PostgresWalCdcTramPipelineFactory postgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                         DataProducerFactory dataProducerFactory,
                                                                         EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                         EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                         EventuateKafkaProducer eventuateKafkaProducer,
                                                                         PublishingFilter publishingFilter,
                                                                         PostgresWalOffsetStoreFactory postgresWalOffsetStoreFactory) {

    return new PostgresWalCdcTramPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter,
            postgresWalOffsetStoreFactory);
  }
}
