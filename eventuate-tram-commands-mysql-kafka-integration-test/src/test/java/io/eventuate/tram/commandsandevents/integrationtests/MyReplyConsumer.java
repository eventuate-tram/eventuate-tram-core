package io.eventuate.tram.commandsandevents.integrationtests;

import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import static java.util.Collections.singleton;

public class MyReplyConsumer {

  public final BlockingDeque<Message> messages = new LinkedBlockingDeque<>();

  @Autowired
  private ChannelMapping channelMapping;

  private MessageConsumer messageConsumer;
  private String replyChannel;

  public MyReplyConsumer(MessageConsumer messageConsumer, String replyChannel) {
    this.messageConsumer = messageConsumer;
    this.replyChannel = replyChannel;
  }

  @PostConstruct
  public void subscribe() {
    messageConsumer.subscribe(getClass().getName(), singleton(channelMapping.transform(replyChannel)), this::handler);
  }

  private void handler(Message message) {
    messages.add(message);
  }

  public String getReplyChannel() {
    return replyChannel;
  }
}
