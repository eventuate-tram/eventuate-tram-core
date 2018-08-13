package io.eventuate.tram.cdc.mysql.connector.configuration.pipeline;

import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestination;
import io.eventuate.tram.cdc.mysql.connector.MysqlBinLogOffsetStoreFactory;
import io.eventuate.tram.cdc.mysql.connector.PostgresWalOffsetStoreFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.factory.MySqlCdcTramPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.factory.PollingCdcPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.factory.PostgresWalCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdcTramPipelineFactoryConfiguration {

  @Bean
  public MySqlCdcTramPipelineFactory createMySqlCdcTramPipelineFactory(CuratorFramework curatorFramework,
                              DataProducerFactory dataProducerFactory,
                              EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                              EventuateKafkaProducer eventuateKafkaProducer,
                              PublishingStrategy<MessageWithDestination> publishingStrategy,
                              MysqlBinLogOffsetStoreFactory mysqlBinLogOffsetStoreFactory,
                              PublishingFilter publishingFilter) {

    return new MySqlCdcTramPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingStrategy,
            mysqlBinLogOffsetStoreFactory,
            publishingFilter);
  }

  @Bean
  public PollingCdcPipelineFactory createPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                   PublishingStrategy<MessageWithDestination> publishingStrategy,
                                                                   DataProducerFactory dataProducerFactory) {

    return new PollingCdcPipelineFactory(curatorFramework, publishingStrategy, dataProducerFactory);
  }

  @Bean
  public PostgresWalCdcPipelineFactory createPostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                           PublishingStrategy<MessageWithDestination> publishingStrategy,
                                                                           DataProducerFactory dataProducerFactory,
                                                                           EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                                           EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                                           EventuateKafkaProducer eventuateKafkaProducer,
                                                                           PublishingFilter publishingFilter,
                                                                           PostgresWalOffsetStoreFactory postgresWalOffsetStoreFactory) {

    return new PostgresWalCdcPipelineFactory(curatorFramework,
            publishingStrategy,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter,
            postgresWalOffsetStoreFactory);
  }
}
