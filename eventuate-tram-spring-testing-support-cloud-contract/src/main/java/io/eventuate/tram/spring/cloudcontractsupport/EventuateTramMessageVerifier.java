package io.eventuate.tram.spring.cloudcontractsupport;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singleton;

public class EventuateTramMessageVerifier implements MessageVerifier<Message> {

  @Autowired
  private MessageProducer messageProducer;
  @Autowired
  private MessageConsumer messageConsumer;

  private ConcurrentHashMap<String, LinkedBlockingQueue<Message>> messagesByDestination = new ConcurrentHashMap<>();

  @PostConstruct
  public void subscribe() {
    messageConsumer.subscribe(getClass().getName(), singleton("*"), m -> {
      String destination = m.getRequiredHeader(Message.DESTINATION);
      getForDestination(destination).add(m);
    });
  }

  private LinkedBlockingQueue<Message> getForDestination(String destination) {
    return messagesByDestination.computeIfAbsent(destination, k -> new LinkedBlockingQueue<>());
  }

  @Override
  public Message receive(String destination, long timeout, TimeUnit timeUnit, @Nullable YamlContract contract) {
    Message m;
    try {
      m = getForDestination(destination).poll(timeout, timeUnit);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return m;
  }

  @Override
  public Message receive(String destination, YamlContract contract) {
    return receive(destination, 5, TimeUnit.SECONDS);
  }

  @Override
  public void send(Message message, String destination, @Nullable YamlContract contract) {
    messageProducer.send(destination, message);
  }

  @Override
  public <T> void send(T payload, Map<String, Object> headers, String destination, @Nullable YamlContract contract) {
    MessageBuilder messageBuilder = MessageBuilder.withPayload(payload.toString());
    headers.forEach((name, value) -> messageBuilder.withHeader(name, value.toString()));
    messageProducer.send(destination, messageBuilder.build());

  }
}
