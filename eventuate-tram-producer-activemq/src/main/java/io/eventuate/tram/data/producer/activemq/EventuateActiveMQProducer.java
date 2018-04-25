package io.eventuate.tram.data.producer.activemq;

import io.eventuate.local.java.common.broker.DataProducer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.CompletableFuture;

public class EventuateActiveMQProducer implements DataProducer {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private Connection connection;
  private Session session;

  public EventuateActiveMQProducer(String url) {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
    try {
      connection = connectionFactory.createConnection();
      connection.start();
      session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
    } catch (JMSException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public CompletableFuture<?> send(String topic, String key, String body) {
    MessageProducer producer = null;
    try {
      Destination destination = session.createQueue(topic);

      producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.PERSISTENT);

      TextMessage message = session.createTextMessage(body);
      producer.send(message);
      producer.close();
    } catch (JMSException e) {
      logger.error(e.getMessage(), e);
    } finally {
      if (producer != null) {
        try {
          producer.close();
        } catch (JMSException e) {
          logger.error(e.getMessage(), e);
        }
      }
    }

    CompletableFuture<Void> result = new CompletableFuture<>();
    result.complete(null);
    return result;
  }

  @Override
  public void close() {
    try {
      connection.close();
      session.close();
    } catch (JMSException e) {
      logger.error(e.getMessage(), e);
    }
  }
}
