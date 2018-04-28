package io.eventuate.tram.consumer.kafka;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumer;
import io.eventuate.tram.consumer.common.DuplicateMessageDetector;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

  @Override
  public void subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> kcHandler = (record, callback) -> {
      Message m = toMessage(record);

      // TODO If we do that here then remove TT from higher-levels

      transactionTemplate.execute(ts -> {
        if (duplicateMessageDetector.isDuplicate(subscriberId, m.getId())) {
          logger.trace("Duplicate message {} {}", subscriberId, m.getId());
          callback.accept(null, null);
          return null;
        }
        try {
          logger.trace("Invoking handler {} {}", subscriberId, m.getId());
          handler.accept(m);
        } catch (Throwable t) {
          logger.trace("Got exception {} {}", subscriberId, m.getId());
          logger.trace("Got exception ", t);
          callback.accept(null, t);
          return null;
        }
        logger.trace("handled message {} {}", subscriberId, m.getId());
        callback.accept(null, null);
        return null;
      });
    };

    EventuateKafkaConsumer kc = new EventuateKafkaConsumer(subscriberId, kcHandler, new ArrayList<>(channels), bootstrapServers);
    consumers.add(kc);
    kc.start();
  }

  public void close() {
    consumers.forEach(EventuateKafkaConsumer::stop);
  }

  private Message toMessage(ConsumerRecord<String, String> record) {
    return JSonMapper.fromJson(record.value(), MessageImpl.class);
  }
}
