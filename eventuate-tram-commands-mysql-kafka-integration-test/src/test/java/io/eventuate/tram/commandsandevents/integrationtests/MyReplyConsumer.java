package io.eventuate.tram.commandsandevents.integrationtests;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static java.util.Collections.singleton;

public class MyReplyConsumer {

  public final BlockingDeque<Message> messages = new LinkedBlockingDeque<>();

  private MessageConsumer messageConsumer;
  private String channel;

  public MyReplyConsumer(MessageConsumer messageConsumer, String channel) {
    this.messageConsumer = messageConsumer;
    this.channel = channel;
  }

  @PostConstruct
  public void subscribe() {
    messageConsumer.subscribe(getClass().getName(), singleton(channel), this::handler);
  }

  private void handler(Message message) {
    messages.add(message);
  }
}
