package io.eventuate.tram.cdc.mysql.connector.pipeline.polling.factory;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.local.common.PublishingStrategy;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.polling.PollingDataProvider;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.AbstractPollingCdcPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestination;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestinationPublishingStrategy;
import io.eventuate.tram.cdc.mysql.connector.PollingMessageBean;
import io.eventuate.tram.cdc.mysql.connector.PollingMessageDataProvider;
import org.apache.curator.framework.CuratorFramework;

public class PollingCdcTramPipelineFactory extends AbstractPollingCdcPipelineFactory<MessageWithDestination, PollingMessageBean, String> {

  public static final String TYPE = "eventuate-tram-event-polling";

  public PollingCdcTramPipelineFactory(CuratorFramework curatorFramework,
                                       DataProducerFactory dataProducerFactory) {

    super(curatorFramework, dataProducerFactory);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }

  @Override
  protected PollingDataProvider<PollingMessageBean, MessageWithDestination, String> createPollingDataProvider(EventuateSchema eventuateSchema) {
    return new PollingMessageDataProvider(eventuateSchema);
  }


  @Override
  protected PublishingStrategy<MessageWithDestination> createPublishingStrategy() {
    return new MessageWithDestinationPublishingStrategy();
  }
}