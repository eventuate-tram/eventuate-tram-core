package io.eventuate.tram.cdc.mysql.connector.pipeline.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.postgres.wal.PostgresWalMessageParser;
import io.eventuate.local.unified.cdc.factory.AbstractPostgresWalCdcPipelineFactory;
import io.eventuate.local.unified.cdc.properties.PostgresWalCdcPipelineProperties;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestination;
import io.eventuate.tram.cdc.mysql.connector.PostgresWalJsonMessageParser;
import io.eventuate.tram.cdc.mysql.connector.PostgresWalOffsetStoreFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class PostgresWalCdcPipelineFactory extends AbstractPostgresWalCdcPipelineFactory<MessageWithDestination> {

  private PostgresWalOffsetStoreFactory postgresWalOffsetStoreFactory;

  public PostgresWalCdcPipelineFactory(CuratorFramework curatorFramework,
                                       PublishingStrategy<MessageWithDestination> publishingStrategy,
                                       DataProducerFactory dataProducerFactory,
                                       EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                       EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                       EventuateKafkaProducer eventuateKafkaProducer,
                                       PublishingFilter publishingFilter,
                                       PostgresWalOffsetStoreFactory postgresWalOffsetStoreFactory) {
    super(curatorFramework,
            publishingStrategy,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingFilter);

    this.postgresWalOffsetStoreFactory = postgresWalOffsetStoreFactory;
  }

  @Override
  protected PostgresWalMessageParser<MessageWithDestination> createPostgresReplicationMessageParser() {
    return new PostgresWalJsonMessageParser();
  }

  @Override
  protected OffsetStore createOffsetStore(PostgresWalCdcPipelineProperties properties,
                                          DataSource dataSource,
                                          EventuateSchema eventuateSchema) {
    return postgresWalOffsetStoreFactory.create(properties,
            new JdbcTemplate(dataSource),
            eventuateSchema);
  }
}
