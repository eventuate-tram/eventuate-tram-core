package io.eventuate.messaging.kafka.basic.consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("eventuate.local.kafka.consumer")
public class EventuateKafkaConsumerConfigurationProperties {
  private Map<String, String> properties = new HashMap<>();

  private BackPressureConfig backPressure = new BackPressureConfig();
  private long pollTimeout;

  public BackPressureConfig getBackPressure() {
    return backPressure;
  }

  public void setBackPressure(BackPressureConfig backPressure) {
    this.backPressure = backPressure;
  }

  public long getPollTimeout() {
    return pollTimeout;
  }

  public void setPollTimeout(long pollTimeout) {
    this.pollTimeout = pollTimeout;
  }
  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public static EventuateKafkaConsumerConfigurationProperties empty() {
    return new EventuateKafkaConsumerConfigurationProperties();
  }

}
