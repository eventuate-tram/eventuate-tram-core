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
import io.eventuate.tram.cdc.mysql.connector.pipeline.factory.PollingCdcTramPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.factory.PostgresWalCdcTramPipelineFactory;
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
                              MysqlBinLogOffsetStoreFactory mysqlBinLogOffsetStoreFactory,
                              PublishingFilter publishingFilter) {

    return new MySqlCdcTramPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            mysqlBinLogOffsetStoreFactory,
            publishingFilter);
  }

  @Bean
  public PollingCdcTramPipelineFactory createPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                       DataProducerFactory dataProducerFactory) {

    return new PollingCdcTramPipelineFactory(curatorFramework, dataProducerFactory);
  }

  @Bean
  public PostgresWalCdcTramPipelineFactory createPostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
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
