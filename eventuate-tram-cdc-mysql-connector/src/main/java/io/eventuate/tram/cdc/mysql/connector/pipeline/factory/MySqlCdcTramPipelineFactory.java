package io.eventuate.tram.cdc.mysql.connector.pipeline.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.db.log.common.OffsetStore;
import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.IWriteRowsEventDataParser;
import io.eventuate.local.mysql.binlog.SourceTableNameSupplier;
import io.eventuate.local.unified.cdc.factory.AbstractMySqlBinlogCdcPipelineFactory;
import io.eventuate.local.unified.cdc.properties.MySqlBinlogCdcPipelineProperties;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestination;
import io.eventuate.tram.cdc.mysql.connector.MySQLTableConfig;
import io.eventuate.tram.cdc.mysql.connector.MysqlBinLogOffsetStoreFactory;
import io.eventuate.tram.cdc.mysql.connector.WriteRowsEventDataParser;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class MySqlCdcTramPipelineFactory extends AbstractMySqlBinlogCdcPipelineFactory<MessageWithDestination> {

  private MysqlBinLogOffsetStoreFactory mysqlBinLogOffsetStoreFactory;

  public MySqlCdcTramPipelineFactory(CuratorFramework curatorFramework,
                                     DataProducerFactory dataProducerFactory,
                                     EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                     EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                     EventuateKafkaProducer eventuateKafkaProducer,
                                     PublishingStrategy<MessageWithDestination> publishingStrategy,
                                     MysqlBinLogOffsetStoreFactory mysqlBinLogOffsetStoreFactory,
                                     PublishingFilter publishingFilter) {
    super(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            publishingStrategy,
            publishingFilter);

    this.mysqlBinLogOffsetStoreFactory = mysqlBinLogOffsetStoreFactory;
  }

  @Override
  protected SourceTableNameSupplier createSourceTableNameSupplier(MySqlBinlogCdcPipelineProperties mySqlBinlogCdcPipelineProperties) {
    return new SourceTableNameSupplier(mySqlBinlogCdcPipelineProperties.getSourceTableName(), MySQLTableConfig.EVENTS_TABLE_NAME);

  }

  @Override
  protected IWriteRowsEventDataParser<MessageWithDestination> createWriteRowsEventDataParser(EventuateSchema eventuateSchema,
                                                                                             DataSource dataSource,
                                                                                             SourceTableNameSupplier sourceTableNameSupplier) {
    return new WriteRowsEventDataParser(dataSource, eventuateSchema);
  }

  @Override
  protected OffsetStore createOffsetStore(MySqlBinlogCdcPipelineProperties properties,
                                          DataSource dataSource,
                                          EventuateSchema eventuateSchema) {

    return mysqlBinLogOffsetStoreFactory.create(properties, new JdbcTemplate(dataSource), eventuateSchema);
  }
}
