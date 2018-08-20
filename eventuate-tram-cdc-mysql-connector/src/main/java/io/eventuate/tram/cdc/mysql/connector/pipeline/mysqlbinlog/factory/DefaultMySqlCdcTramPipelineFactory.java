package io.eventuate.tram.cdc.mysql.connector.pipeline.mysqlbinlog.factory;

import io.eventuate.local.db.log.common.PublishingFilter;
import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.java.kafka.EventuateKafkaConfigurationProperties;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.local.java.kafka.producer.EventuateKafkaProducer;
import io.eventuate.tram.cdc.mysql.connector.MysqlBinLogOffsetStoreFactory;
import org.apache.curator.framework.CuratorFramework;

public class DefaultMySqlCdcTramPipelineFactory extends MySqlCdcTramPipelineFactory {

  public static final String TYPE = "default-eventuate-tram-mysql-binlog";

  public DefaultMySqlCdcTramPipelineFactory(CuratorFramework curatorFramework,
                                            DataProducerFactory dataProducerFactory,
                                            EventuateKafkaConfigurationProperties eventuateKafkaConfigurationProperties,
                                            EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties,
                                            EventuateKafkaProducer eventuateKafkaProducer,
                                            MysqlBinLogOffsetStoreFactory mysqlBinLogOffsetStoreFactory,
                                            PublishingFilter publishingFilter) {
    super(curatorFramework,
            dataProducerFactory,
            eventuateKafkaConfigurationProperties,
            eventuateKafkaConsumerConfigurationProperties,
            eventuateKafkaProducer,
            mysqlBinLogOffsetStoreFactory,
            publishingFilter);
  }

  @Override
  public boolean supports(String type) {
    return TYPE.equals(type);
  }
}
