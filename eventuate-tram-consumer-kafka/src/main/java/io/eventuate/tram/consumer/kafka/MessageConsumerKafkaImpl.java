package io.eventuate.tram.consumer.kafka;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumer;
import io.eventuate.local.java.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
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
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    SwimlaneBasedDispatcher swimlaneBasedDispatcher = new SwimlaneBasedDispatcher(subscriberId, Executors.newCachedThreadPool());
    TransactionalMessageHandler transactionalMessageHandler = new TransactionalMessageHandler(subscriberId, handler, messageInterceptors, duplicateMessageDetector, transactionTemplate);
    BiConsumer<ConsumerRecord<String, String>, BiConsumer<Void, Throwable>> kcHandler = (record, callback) -> {
      swimlaneBasedDispatcher.dispatch(toMessage(record), record.partition(), message -> transactionalMessageHandler.handle(message, callback));
    };

    EventuateKafkaConsumer kc = new EventuateKafkaConsumer(subscriberId,
            kcHandler,
            new ArrayList<>(channels),
            bootstrapServers,
            eventuateKafkaConsumerConfigurationProperties);

    consumers.add(kc);

    kc.start();

    return () -> {
      kc.stop();
      consumers.remove(kc);
    };

  }


  public void close() {
    consumers.forEach(EventuateKafkaConsumer::stop);
  }

  private Message toMessage(ConsumerRecord<String, String> record) {
    return JSonMapper.fromJson(record.value(), MessageImpl.class);
  }
}
