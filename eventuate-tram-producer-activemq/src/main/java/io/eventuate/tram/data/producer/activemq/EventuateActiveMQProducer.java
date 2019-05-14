package io.eventuate.tram.data.producer.activemq;

import io.eventuate.local.java.common.broker.DataProducer;
import io.eventuate.tram.messaging.common.ChannelType;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EventuateActiveMQProducer implements DataProducer {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private Connection connection;
  private Session session;
  private Map<String, ChannelType> messageModes;

  public EventuateActiveMQProducer(String url) {
    this(url, Collections.emptyMap());
  }

  public EventuateActiveMQProducer(String url, Optional<String> user, Optional<String> password) {
    this(url, Collections.emptyMap(), user, password);
  }


  public EventuateActiveMQProducer(String url, Map<String, ChannelType> messageModes) {
    this(url, messageModes, Optional.empty(), Optional.empty());
  }

  public EventuateActiveMQProducer(String url,
                                   Map<String, ChannelType> messageModes,
                                   Optional<String> user,
                                   Optional<String> password) {

    this.messageModes = messageModes;
    ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory(url, user, password);
    try {
      connection = connectionFactory.createConnection();
      connection.setExceptionListener(e -> logger.error(e.getMessage(), e));
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
      ChannelType mode = messageModes.getOrDefault(topic, ChannelType.TOPIC);

      Destination destination = mode == ChannelType.TOPIC ?
              session.createTopic("VirtualTopic." + topic) :
              session.createQueue(topic);

      producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.PERSISTENT);

      TextMessage message = session.createTextMessage(body);
      message.setStringProperty("JMSXGroupID", key);
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

    return CompletableFuture.completedFuture(null);
  }

  private ActiveMQConnectionFactory createActiveMQConnectionFactory(String url, Optional<String> user, Optional<String> password) {
    return user
            .flatMap(usr -> password.flatMap(pass ->
                    Optional.of(new ActiveMQConnectionFactory(usr, pass, url))))
            .orElseGet(() -> new ActiveMQConnectionFactory(url));
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
