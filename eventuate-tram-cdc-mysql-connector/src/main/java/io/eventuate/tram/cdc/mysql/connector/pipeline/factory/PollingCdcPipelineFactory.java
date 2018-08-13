package io.eventuate.tram.cdc.mysql.connector.pipeline.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.local.unified.cdc.factory.AbstractPollingCdcPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestination;
import io.eventuate.tram.cdc.mysql.connector.PollingMessageBean;
import io.eventuate.tram.cdc.mysql.connector.PollingMessageDataProvider;
import org.apache.curator.framework.CuratorFramework;

public class PollingCdcPipelineFactory extends AbstractPollingCdcPipelineFactory<MessageWithDestination, PollingMessageBean, String> {

  public PollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                   PublishingStrategy<MessageWithDestination> publishingStrategy,
                                   DataProducerFactory dataProducerFactory) {

    super(curatorFramework, publishingStrategy, dataProducerFactory);
  }

  @Override
  protected PollingDataProvider<PollingMessageBean, MessageWithDestination, String> createPollingDataProvider(EventuateSchema eventuateSchema) {
    return new PollingMessageDataProvider(eventuateSchema);
  }
}