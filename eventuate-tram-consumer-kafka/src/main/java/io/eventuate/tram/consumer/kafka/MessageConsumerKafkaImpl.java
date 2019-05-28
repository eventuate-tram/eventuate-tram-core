package io.eventuate.tram.consumer.kafka;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.common.kafka.consumer.EventuateKafkaConsumer;
import io.eventuate.common.kafka.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.common.kafka.consumer.EventuateKafkaConsumerMessageHandler;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.consumer.common.SubscriberIdAndMessage;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MessageConsumerKafkaImpl implements MessageConsumerImplementation {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final String id = UUID.randomUUID().toString();

  private String bootstrapServers;
  private List<EventuateKafkaConsumer> consumers = new ArrayList<>();

  public MessageConsumerKafkaImpl(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  @Autowired
  private EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties;

  @Autowired
  private DecoratedMessageHandlerFactory decoratedMessageHandlerFactory;

  @Override
  public MessageSubscription subscribe(String subscriberId, Set<String> channels, MessageHandler handler) {
    Consumer<SubscriberIdAndMessage> decoratedHandler = decoratedMessageHandlerFactory.decorate(handler);

    SwimlaneBasedDispatcher swimlaneBasedDispatcher = new SwimlaneBasedDispatcher(subscriberId, Executors.newCachedThreadPool());

    EventuateKafkaConsumerMessageHandler kcHandler = (record, callback) -> {
      swimlaneBasedDispatcher.dispatch(toMessage(record), record.partition(), message -> handle(message, callback, subscriberId, decoratedHandler));
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

  public void handle(Message message, BiConsumer<Void, Throwable> callback, String subscriberId, Consumer<SubscriberIdAndMessage> decoratedHandler) {
    try {
      decoratedHandler.accept(new SubscriberIdAndMessage(subscriberId, message));
      callback.accept(null, null);
    } catch (Throwable e) {
      callback.accept(null, e);
      throw e;
    }
  }


  public void close() {
    consumers.forEach(EventuateKafkaConsumer::stop);
  }

  private Message toMessage(ConsumerRecord<String, String> record) {
    return JSonMapper.fromJson(record.value(), MessageImpl.class);
  }

  @Override
  public String getId() {
    return id;
  }
}
