package io.eventuate.tram.cdc.mysql.connector.configuration;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.CdcProcessor;
import io.eventuate.local.common.EventuateConfigurationProperties;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.PollingCdcDataPublisher;
import io.eventuate.local.polling.PollingCdcProcessor;
import io.eventuate.local.polling.PollingDao;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestination;
import io.eventuate.tram.cdc.mysql.connector.PollingMessageBean;
import io.eventuate.tram.cdc.mysql.connector.PollingMessageDataProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class EventuatePollingMessageTableChangesToDestinationsConfiguration {
  @Bean
  @Profile("EventuatePolling")
  public CdcDataPublisher<MessageWithDestination> pollingCdcDataPublisher(DataProducerFactory dataProducerFactory,
                                                                          PublishingStrategy<MessageWithDestination> publishingStrategy) {

    return new PollingCdcDataPublisher<>(dataProducerFactory, publishingStrategy);
  }

  @Bean
  @Profile("EventuatePolling")
  public CdcProcessor<MessageWithDestination> pollingCdcProcessor(EventuateConfigurationProperties eventuateConfigurationProperties,
                                                                  PollingDao<PollingMessageBean, MessageWithDestination, String> pollingDao) {

    return new PollingCdcProcessor<>(pollingDao, eventuateConfigurationProperties.getPollingIntervalInMilliseconds());
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDao<PollingMessageBean, MessageWithDestination, String> pollingDao(PollingDataProvider<PollingMessageBean, MessageWithDestination, String> pollingDataProvider,
                                                                                   DataSource dataSource,
                                                                                   EventuateConfigurationProperties eventuateConfigurationProperties) {

    return new PollingDao<>(pollingDataProvider,
            dataSource,
            eventuateConfigurationProperties.getMaxEventsPerPolling(),
            eventuateConfigurationProperties.getMaxAttemptsForPolling(),
            eventuateConfigurationProperties.getPollingRetryIntervalInMilliseconds());
  }

  @Bean
  @Profile("EventuatePolling")
  public PollingDataProvider<PollingMessageBean, MessageWithDestination, String> pollingDataProvider(EventuateSchema eventuateSchema) {
    return new PollingMessageDataProvider(eventuateSchema);
  }
}
