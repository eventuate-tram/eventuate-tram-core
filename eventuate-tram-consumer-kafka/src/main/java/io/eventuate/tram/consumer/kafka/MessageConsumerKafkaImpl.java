package io.eventuate.tram.consumer.kafka;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumer;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class MessageConsumerKafkaImpl implements MessageConsumer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private String bootstrapServers;
  private List<EventuateKafkaConsumer> consumers = new ArrayList<>();

  public MessageConsumerKafkaImpl(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private DuplicateMessageDetector duplicateMessageDetector;

  @Autowired
  private EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;

  @Autowired(required = false)
  private MessageInterceptor[] messageInterceptors = new MessageInterceptor[0];

  @Override
  public void subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    SwimlaneBasedDispatcher swimlaneBasedDispatcher = new SwimlaneBasedDispatcher(subscriberId, Executors.newCachedThreadPool());

    BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> kcHandler = (record, callback) -> {
      swimlaneBasedDispatcher.dispatch(toMessage(record), record.partition(), message -> {
                preReceive(message);
                try {
                  transactionTemplate.execute(ts -> {
                    if (duplicateMessageDetector.isDuplicate(subscriberId, message.getId())) {
                      logger.trace("Duplicate message {} {}", subscriberId, message.getId());
                      callback.accept(null, null);
                      return null;
                    }
                    try {
                      logger.trace("Invoking handler {} {}", subscriberId, message.getId());
                      preHandle(subscriberId, message);
                      handler.accept(message);
                      postHandle(subscriberId, message, null);
                    } catch (Throwable t) {
                      postHandle(subscriberId, message, t);
                      logger.trace("Got exception {} {}", subscriberId, message.getId());
                      logger.trace("Got exception ", t);
                      callback.accept(null, t);
                      return null;
                    }
                    logger.trace("handled message {} {}", subscriberId, message.getId());
                    callback.accept(null, null);
                    return null;
                  });
                } finally {
                  postReceive(message);
                }
              }

      );
    };

    EventuateKafkaConsumer kc = new EventuateKafkaConsumer(subscriberId,
            kcHandler,
            new ArrayList<>(channels),
            bootstrapServers,
            eventuateKafkaConsumerConfigurationProperties);

    consumers.add(kc);

    kc.start();
  }

  protected void preReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postReceive(message));
  }

  private void postHandle(String subscriberId, Message message, Throwable t) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postHandle(subscriberId, message, t));
  }

  private void preHandle(String subscriberId, Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.preHandle(subscriberId, message));
  }

  protected void postReceive(Message message) {
    Arrays.stream(messageInterceptors).forEach(mi -> mi.postReceive(message));
  }


  public void close() {
    consumers.forEach(EventuateKafkaConsumer::stop);
  }

  private Message toMessage(ConsumerRecord<String, String> record) {
    return JSonMapper.fromJson(record.value(), MessageImpl.class);
  }
}
