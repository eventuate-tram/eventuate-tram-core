package io.eventuate.tram.cdc.mysql.connector.pipeline.mysqlbinlog.configuration;

import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.tram.cdc.mysql.connector.MysqlBinLogOffsetStoreFactory;
import io.eventuate.tram.cdc.mysql.connector.pipeline.mysqlbinlog.factory.MySqlCdcTramPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySqlBinlogCdcTramPipelineFactoryConfiguration {
  @Bean("eventuateTramMySqlBinlog")
  public MySqlCdcTramPipelineFactory mySqlCdcTramPipelineFactory(CuratorFramework curatorFramework,
                              DataProducerFactory dataProducerFactory,
                              EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                              EventuateKafkaProducer eventuateKafkaProducer,
                              MysqlBinLogOffsetStoreFactory mysqlBinLogOffsetStoreFactory,
                              PublishingFilter publishingFilter) {

    return new MySqlCdcTramPipelineFactory(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            mysqlBinLogOffsetStoreFactory,
            publishingFilter);
  }
}
