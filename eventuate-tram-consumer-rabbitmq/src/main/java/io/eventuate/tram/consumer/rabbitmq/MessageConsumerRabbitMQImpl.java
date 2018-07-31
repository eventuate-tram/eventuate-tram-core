package io.eventuate.tram.consumer.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class MessageConsumerRabbitMQImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  private Connection connection;
  private int partitionCount;
  private String zkUrl;

  private List<Subscription> subscriptions = new ArrayList<>();

  public MessageConsumerRabbitMQImpl(String rabbitMQUrl, String zkUrl, int partitionCount) {
    this.partitionCount = partitionCount;
    this.zkUrl = zkUrl;
    prepareRabbitMQConnection(rabbitMQUrl);
  }

  private void prepareRabbitMQConnection(String rabbitMQUrl) {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitMQUrl);
    try {
      connection = factory.newConnection();
    } catch (IOException | TimeoutException e) {
      logger.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    Subscription subscription = new Subscription(connection,
            zkUrl,
            subscriberId,
            channels,
            partitionCount,
            (message, acknowledgeCallback) -> handleMessage(subscriberId, handler, message, acknowledgeCallback));

    subscriptions.add(subscription);
  }

  private void handleMessage(String subscriberId, MessageHandler handler, Message tramMessage, Runnable acknowledgeCallback) {
    transactionTemplate.execute(ts -> {
      if (duplicateMessageDetector.isDuplicate(subscriberId, tramMessage.getId())) {
        logger.trace("Duplicate message {} {}", subscriberId, tramMessage.getId());
        acknowledgeCallback.run();
        return null;
      }

      try {
        logger.trace("Invoking handler {} {}", subscriberId, tramMessage.getId());
        handler.accept(tramMessage);
        logger.trace("handled message {} {}", subscriberId, tramMessage.getId());
      } catch (Throwable t) {
        logger.trace("Got exception {} {}", subscriberId, tramMessage.getId());
        logger.trace("Got exception ", t);
      } finally {
        acknowledgeCallback.run();
      }

      return null;
    });
  }

  public void close() {
    subscriptions.forEach(Subscription::close);

    try {
      connection.close();
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }
}