package io.eventuate.tram.cdc.mysql.connector.pipeline;

import io.eventuate.local.unified.cdc.pipeline.common.BinlogEntryReaderProvider;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.eventuate.tram.cdc.mysql.connector.BinlogEntryToMessageConverter;
import io.eventuate.tram.cdc.mysql.connector.MessageWithDestinationPublishingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultCdcTramPipelineFactoryConfiguration {
  @Bean("defaultCdcPipelineFactory")
  public CdcPipelineFactory defaultCdcPipelineFactory(BinlogEntryReaderProvider binlogEntryReaderProvider) {

    return new CdcPipelineFactory<>("eventuate-tram",
            binlogEntryReaderProvider,
            new BinlogEntryToMessageConverter(),
            new MessageWithDestinationPublishingStrategy());
  }
}
