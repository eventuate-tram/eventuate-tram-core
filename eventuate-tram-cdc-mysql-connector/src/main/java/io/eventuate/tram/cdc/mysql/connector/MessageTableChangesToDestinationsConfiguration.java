package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.DatabaseBinlogOffsetKafkaStore;
import io.eventuate.local.mysql.binlog.EventTableChangesToAggregateTopicTranslator;
import io.eventuate.local.mysql.binlog.EventuateLocalZookeperConfigurationProperties;
import io.eventuate.local.mysql.binlog.IWriteRowsEventDataParser;
import io.eventuate.local.mysql.binlog.JdbcUrl;
import io.eventuate.local.mysql.binlog.JdbcUrlParser;
import io.eventuate.local.mysql.binlog.MySQLCdcKafkaPublisher;
import io.eventuate.local.mysql.binlog.MySQLCdcProcessor;
import io.eventuate.local.mysql.binlog.MySqlBinaryLogClient;
import io.eventuate.local.mysql.binlog.MySqlBinaryLogClientConfigurationProperties;
import io.eventuate.local.mysql.binlog.PublishingStrategy;
import io.eventuate.local.mysql.binlog.SourceTableNameSupplier;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
@EnableConfigurationProperties({MySqlBinaryLogClientConfigurationProperties.class, EventuateLocalZookeperConfigurationProperties.class})
@Import(EventuateDriverConfiguration.class)
public class MessageTableChangesToDestinationsConfiguration {

  @Bean
  public SourceTableNameSupplier sourceTableNameSupplier(MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties) {
    return new SourceTableNameSupplier(mySqlBinaryLogClientConfigurationProperties.getSourceTableName(), MySQLTableConfig.EVENTS_TABLE_NAME);
  }

  @Bean
  public IWriteRowsEventDataParser eventDataParser(DataSource dataSource) {
    return new WriteRowsEventDataParser(dataSource);
  }

  @Bean
  public MySqlBinaryLogClient<MessageWithDestination> mySqlBinaryLogClient(@Value("${spring.datasource.url}") String dataSourceURL,
                                                                           MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties,
                                                                           SourceTableNameSupplier sourceTableNameSupplier,
                                                                           IWriteRowsEventDataParser<MessageWithDestination> eventDataParser) throws IOException, TimeoutException {
    JdbcUrl jdbcUrl = JdbcUrlParser.parse(dataSourceURL);
    return new MySqlBinaryLogClient<>(eventDataParser,
            mySqlBinaryLogClientConfigurationProperties.getDbUserName(),
            mySqlBinaryLogClientConfigurationProperties.getDbPassword(),
            jdbcUrl.getHost(),
            jdbcUrl.getPort(),
            mySqlBinaryLogClientConfigurationProperties.getBinlogClientId(),
            sourceTableNameSupplier.getSourceTableName());
  }

  @Bean
  public EventuateKafkaProducer eventuateKafkaProducer(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties) {
    return new EventuateKafkaProducer(eventuateKafkaConfigurationProperties.getBootstrapServers());
  }

  @Bean
  public MySQLCdcKafkaPublisher<MessageWithDestination> mySQLCdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties, DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore, PublishingStrategy<MessageWithDestination> publishingStrategy) {
    return new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore, eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }

  @Bean
  public PublishingStrategy<MessageWithDestination> publishingStrategy() {
    return new MessageWithDestinationPublishingStrategy();
  }

  @Bean
  public MySQLCdcProcessor<MessageWithDestination> mySQLCdcProcessor(MySqlBinaryLogClient<MessageWithDestination> mySqlBinaryLogClient, DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore) {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore);
  }

  @Bean
  public DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                               MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties,
                                                               MySqlBinaryLogClient mySqlBinaryLogClient,
                                                               EventuateKafkaProducer eventuateKafkaProducer) {
    return new DatabaseBinlogOffsetKafkaStore(mySqlBinaryLogClientConfigurationProperties.getDbHistoryTopicName(), mySqlBinaryLogClient.getName(), eventuateKafkaProducer, eventuateKafkaConfigurationProperties);
  }

  @Bean
  public EventTableChangesToAggregateTopicTranslator<MessageWithDestination> eventTableChangesToAggregateTopicTranslator(MySQLCdcKafkaPublisher<MessageWithDestination> mySQLCdcKafkaPublisher,
                                                                                                                         MySQLCdcProcessor<MessageWithDestination> mySQLCdcProcessor,
                                                                                                                         CuratorFramework curatorFramework) {
    return new EventTableChangesToAggregateTopicTranslator<>(mySQLCdcKafkaPublisher, mySQLCdcProcessor, curatorFramework);
  }

  @Bean(destroyMethod = "close")
  public CuratorFramework curatorFramework(EventuateLocalZookeperConfigurationProperties eventuateLocalZookeperConfigurationProperties) {
    String connectionString = eventuateLocalZookeperConfigurationProperties.getConnectionString();
    return makeStartedCuratorClient(connectionString);
  }

  static CuratorFramework makeStartedCuratorClient(String connectionString) {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.
            builder().retryPolicy(retryPolicy)
            .connectString(connectionString)
            .build();
    client.start();
    return client;
  }


}
