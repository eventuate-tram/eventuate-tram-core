package io.eventuate.tram.cdc.mysql.connector.pipeline.postgreswal.configuration;

import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.unified.cdc.pipeline.common.DefaultPipelineTypeSupplier;
import io.eventuate.tram.cdc.mysql.connector.PostgresWalOffsetStoreFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.postgreswal.factory.DefaultPostgresWalCdcTramPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.postgreswal.factory.PostgresWalCdcTramPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class PostgresWalCdcTramPipelineFactoryConfiguration {

  @Profile("PostgresWal")
  @Primary
  @Bean
  public DefaultPipelineTypeSupplier defaultPipelineTypeSupplier() {
    return () -> DefaultPostgresWalCdcTramPipelineFactory.TYPE;
  }

  @Bean
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

  @Bean
  public DefaultPostgresWalCdcTramPipelineFactory defaultPostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                                       DataProducerFactory dataProducerFactory,
                                                                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                                       EventuateKafkaProducer eventuateKafkaProducer,
                                                                                       PublishingFilter publishingFilter,
                                                                                       PostgresWalOffsetStoreFactory postgresWalOffsetStoreFactory) {

    return new DefaultPostgresWalCdcTramPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter,
            postgresWalOffsetStoreFactory);
  }
}
