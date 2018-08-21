package io.eventuate.tram.cdc.mysql.connector.pipeline.polling.configuration;

import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.polling.factory.PollingCdcTramPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingDefaultCdcTramPipelineFactoryConfiguration {
  @Profile("EventuatePolling")
  @Primary
  @Bean("default")
  public CdcPipelineFactory defaultPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                             DataProducerFactory dataProducerFactory) {

    return new PollingCdcTramPipelineFactory(curatorFramework, dataProducerFactory);
  }
}
