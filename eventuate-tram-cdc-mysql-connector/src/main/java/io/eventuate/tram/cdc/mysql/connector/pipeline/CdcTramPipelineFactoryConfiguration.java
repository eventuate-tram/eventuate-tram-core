package io.eventuate.tram.cdc.mysql.connector.pipeline;

import io.eventuate.local.common.CdcDataPublisher;
import io.eventuate.local.common.DuplicatePublishingDetector;
import io.eventuate.local.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.BinlogEntryToMessageConverter;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestinationPublishingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdcTramPipelineFactoryConfiguration {
  @Bean("eventuateTramСdcPipelineFactory")
  public CdcPipelineFactory cdcPipelineFactory(DataProducerFactory dataProducerFactory,
                                               PublishingFilter publishingFilter,
                                               BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new CdcPipelineFactory<>("eventuate-tram",
            binlogEntryReaderProvider,
            new CdcDataPublisher<>(dataProducerFactory,
                    publishingFilter,
                    new MessageWithDestinationPublishingStrategy()),
            new BinlogEntryToMessageConverter());
  }
}
