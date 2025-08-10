package io.eventuate.tram.reactive.commands;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageHandler;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.util.test.async.Eventually;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ReactiveTestMessageConsumer implements ReactiveMessageHandler {

  private final LinkedBlockingDeque<Message> messages = new LinkedBlockingDeque<>();

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final String replyChannel;

  public ReactiveTestMessageConsumer(String replyChannel) {
    this.replyChannel = replyChannel;
  }

  public String getReplyChannel() {
    return replyChannel;
  }

  @Override
  public Publisher<?> apply(Message message) {
    logger.debug("Got message: {}", message);
    messages.add(message);
    return Mono.empty();
  }

  public boolean containsReplyTo(String messageId) {
    for (Message m : messages.toArray(new Message[0])) {
      if (m.getHeader(ReplyMessageHeaders.IN_REPLY_TO).map(x -> x.equals(messageId)).orElse(false))
        return true;
    }
    return false;
  }

  public void assertHasReplyTo(String messageId) {
    Eventually.eventually(() -> assertTrue(containsReplyTo(messageId)));
  }

  public static ReactiveTestMessageConsumer subscribeTo(ReactiveMessageConsumer messageConsumer, String replyTo) {
    ReactiveTestMessageConsumer testMessageConsumer = new ReactiveTestMessageConsumer(replyTo);
    messageConsumer.subscribe("subscriberId", Collections.singleton(replyTo), testMessageConsumer);
    return testMessageConsumer;
  }

  public void assertContainsMessage(Message message) {
    for (Message m : messages.toArray(new Message[0])) {
      if (m.getHeader(Message.ID).equals(message.getHeader(Message.ID)))
        return;
    }
    fail("Does not contain message: " + message);
  }
}
