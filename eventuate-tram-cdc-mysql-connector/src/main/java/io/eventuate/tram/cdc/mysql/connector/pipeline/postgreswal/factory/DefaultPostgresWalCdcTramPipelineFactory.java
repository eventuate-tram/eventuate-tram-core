package io.eventuate.tram.cdc.mysql.connector.pipeline.postgreswal.factory;

import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.tram.cdc.mysql.connector.PostgresWalOffsetStoreFactory;
import org.apache.curator.framework.CuratorFramework;

public class DefaultPostgresWalCdcTramPipelineFactory extends PostgresWalCdcTramPipelineFactory {

  public static final String TYPE = "default-eventuate-tram-postgres-wal";

  public DefaultPostgresWalCdcTramPipelineFactory(CuratorFramework curatorFramework,
                                                  DataProducerFactory dataProducerFactory,
                                                  EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                  EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                                  EventuateKafkaProducer eventuateKafkaProducer,
                                                  PublishingFilter publishingFilter,
                                                  PostgresWalOffsetStoreFactory postgresWalOffsetStoreFactory) {
    super(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter,
            postgresWalOffsetStoreFactory);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }
}
