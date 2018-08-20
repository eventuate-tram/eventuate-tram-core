package io.eventuate.tram.cdc.mysql.connector.pipeline.polling.configuration;

import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.DefaultPipelineTypeSupplier;
import io.eventuate.tram.cdc.mysql.connector.pipeline.polling.factory.DefaultPollingCdcTramPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.polling.factory.PollingCdcTramPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class PollingCdcTramPipelineFactoryConfiguration {

  @Profile("EventuatePolling")
  @Primary
  @Bean
  public DefaultPipelineTypeSupplier defaultPipelineTypeSupplier() {
    return () -> DefaultPollingCdcTramPipelineFactory.TYPE;
  }

  @Bean
  public PollingCdcTramPipelineFactory pollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                 DataProducerFactory dataProducerFactory) {

    return new PollingCdcTramPipelineFactory(curatorFramework, dataProducerFactory);
  }

  @Bean
  public DefaultPollingCdcTramPipelineFactory defaultPollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                                               DataProducerFactory dataProducerFactory) {

    return new DefaultPollingCdcTramPipelineFactory(curatorFramework, dataProducerFactory);
  }
}
