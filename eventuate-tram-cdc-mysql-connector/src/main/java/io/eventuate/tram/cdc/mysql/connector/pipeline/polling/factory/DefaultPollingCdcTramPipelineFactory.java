package io.eventuate.tram.cdc.mysql.connector.pipeline.polling.factory;

import io.eventuate.local.java.common.broker.DataProducerFactory;
import org.apache.curator.framework.CuratorFramework;

public class DefaultPollingCdcTramPipelineFactory extends PollingCdcTramPipelineFactory {

  public static final String TYPE = "default-eventuate-tram-event-polling";

  public DefaultPollingCdcTramPipelineFactory(CuratorFramework curatorFramework,
                                              DataProducerFactory dataProducerFactory) {

    super(curatorFramework, dataProducerFactory);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }
}