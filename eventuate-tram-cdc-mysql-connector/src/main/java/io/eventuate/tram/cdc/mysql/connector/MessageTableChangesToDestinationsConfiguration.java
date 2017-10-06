package io.eventuate.tram.cdc.mysql.connector;

import io.eventuate.javaclient.driver.EventuateDriverConfiguration;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.local.mysql.binlog.*;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
@EnableConfigurationProperties({MySqlBinaryLogClientConfigurationProperties.class, EventuateLocalZookeperConfigurationProperties.class}
)
@Import(EventuateDriverConfiguration.class)
public class MessageTableChangesToDestinationsConfiguration {

  @Bean
  @Profile("!EventuatePolling")
  public SourceTableNameSupplier sourceTableNameSupplier(MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties) {
    return new SourceTableNameSupplier(mySqlBinaryLogClientConfigurationProperties.getSourceTableName(), MySQLTableConfig.EVENTS_TABLE_NAME);
  }

  @Bean
  @Profile("!EventuatePolling")
  public IWriteRowsEventDataParser eventDataParser(DataSource dataSource) {
    return new WriteRowsEventDataParser(dataSource);
  }

  @Bean
    @Profile("!EventuatePolling")

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
  @Profile("!EventuatePolling")
  public MySQLCdcKafkaPublisher<MessageWithDestination> mySQLCdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties, DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore, PublishingStrategy<MessageWithDestination> publishingStrategy) {
    return new MySQLCdcKafkaPublisher<>(binlogOffsetKafkaStore, eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }

  @Bean
  public PublishingStrategy<MessageWithDestination> publishingStrategy() {
    return new MessageWithDestinationPublishingStrategy();
  }

  @Bean
  @Profile("!EventuatePolling")
  public MySQLCdcProcessor<MessageWithDestination> mySQLCdcProcessor(MySqlBinaryLogClient<MessageWithDestination> mySqlBinaryLogClient, DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore) {
    return new MySQLCdcProcessor<>(mySqlBinaryLogClient, binlogOffsetKafkaStore);
  }

  @Bean
  @Profile("!EventuatePolling")
  public DatabaseBinlogOffsetKafkaStore binlogOffsetKafkaStore(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                                               MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties,
                                                               MySqlBinaryLogClient mySqlBinaryLogClient,
                                                               EventuateKafkaProducer eventuateKafkaProducer) {
    return new DatabaseBinlogOffsetKafkaStore(mySqlBinaryLogClientConfigurationProperties.getDbHistoryTopicName(), mySqlBinaryLogClient.getName(), eventuateKafkaProducer, eventuateKafkaConfigurationProperties);
  }

  @Bean
  @Profile("!EventuatePolling")
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


  @Bean
  @Profile("EventuatePolling")
  public PollingCdcKafkaPublisher<MessageWithDestination> зollingCdcKafkaPublisher(EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
    PublishingStrategy<MessageWithDestination> publishingStrategy) {

    return new PollingCdcKafkaPublisher<>(eventuateKafkaConfigurationProperties.getBootstrapServers(), publishingStrategy);
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingCdcProcessor<PollingMessageBean, MessageWithDestination, String> pollingCdcProcessor(MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties,
    PollingDao<PollingMessageBean, MessageWithDestination, String> pollingDao) {

    return new PollingCdcProcessor<>(pollingDao, mySqlBinaryLogClientConfigurationProperties.getPollingRequestPeriodInMilliseconds());
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingEventTableChangesToAggregateTopicTranslator<PollingMessageBean, MessageWithDestination, String> pollingEventTableChangesToAggregateTopicTranslator(PollingCdcKafkaPublisher<MessageWithDestination> pollingCdcKafkaPublisher,
    PollingCdcProcessor<PollingMessageBean, MessageWithDestination, String> pollingCdcProcessor,
    CuratorFramework curatorFramework) {

    return new PollingEventTableChangesToAggregateTopicTranslator<>(pollingCdcKafkaPublisher, pollingCdcProcessor, curatorFramework);
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDao<PollingMessageBean, MessageWithDestination, String> pollingDao(PollingDataProvider<PollingMessageBean, MessageWithDestination, String> pollingDataProvider,
    DataSource dataSource,
    MySqlBinaryLogClientConfigurationProperties mySqlBinaryLogClientConfigurationProperties) {

    return new PollingDao<>(pollingDataProvider,
      dataSource,
      mySqlBinaryLogClientConfigurationProperties.getMaxEventsPerPolling(),
      mySqlBinaryLogClientConfigurationProperties.getMaxAttemptsForPolling(),
      mySqlBinaryLogClientConfigurationProperties.getDelayPerPollingAttemptInMilliseconds());
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDataProvider<PollingMessageBean, MessageWithDestination, String> pollingDataProvider() {
    return new PollingMessageDataProvider();
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
