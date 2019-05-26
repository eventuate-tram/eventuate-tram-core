package io.eventuate.tram.data.producer.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.eventuate.local.java.common.broker.DataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class EventuateRabbitMQProducer implements DataProducer {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private Connection connection;
  private Channel channel;

  public EventuateRabbitMQProducer(String url) {

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
      AMQP.BasicProperties bp = new AMQP.BasicProperties.Builder().headers(Collections.singletonMap("key", key)).build();

      channel.exchangeDeclare(topic, "fanout");
      channel.basicPublish(topic, key, bp, body.getBytes("UTF-8"));

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return CompletableFuture.completedFuture(null);
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
