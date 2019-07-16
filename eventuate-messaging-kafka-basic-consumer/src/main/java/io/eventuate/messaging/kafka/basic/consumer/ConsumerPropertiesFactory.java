package io.eventuate.messaging.kafka.basic.consumer;

import java.util.Properties;

public class ConsumerPropertiesFactory {
  public static Properties makeDefaultConsumerProperties(String bootstrapServers, String subscriberId) {
    Properties consumerProperties = new Properties();
    consumerProperties.put("bootstrap.servers", bootstrapServers);
    consumerProperties.put("group.id", subscriberId);
    consumerProperties.put("enable.auto.commit", "false");
    //consumerProperties.put("auto.commit.interval.ms", "1000");
    consumerProperties.put("session.timeout.ms", "30000");
    consumerProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProperties.put("auto.offset.reset", "earliest");
    return consumerProperties;
  }
}
