package io.eventuate.tram.data.producer.rabbitmq;

import com.rabbitmq.client.*;
import io.eventuate.local.java.common.broker.DataProducer;
import io.eventuate.tram.messaging.common.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class EventuateRabbitMQProducer implements DataProducer {
  private Logger logger = LoggerFactory.getLogger(getClass());
  private Map<String, ChannelType> messageModes;

  private Connection connection;
  private Channel channel;

  public EventuateRabbitMQProducer(String url) {
    this(url, Collections.emptyMap());
  }

  public EventuateRabbitMQProducer(String url, Map<String, ChannelType> messageModes) {
    this.messageModes = messageModes;

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(url);
    try {
      connection = factory.newConnection();
      channel = connection.createChannel();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public CompletableFuture<?> send(String topic, String key, String body) {
    try {
      ChannelType channelType = messageModes.getOrDefault(topic, ChannelType.TOPIC);

      AMQP.BasicProperties bp = new AMQP.BasicProperties.Builder().headers(Collections.singletonMap("key", key)).build();

      channel.exchangeDeclare(topic, "fanout");
      channel.basicPublish(topic, "", bp, body.getBytes("UTF-8"));

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }

    CompletableFuture<Void> result = new CompletableFuture<>();
    result.complete(null);
    return result;
  }

  @Override
  public void close() {
    try {
      channel.close();
      connection.close();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
    }
  }
}
