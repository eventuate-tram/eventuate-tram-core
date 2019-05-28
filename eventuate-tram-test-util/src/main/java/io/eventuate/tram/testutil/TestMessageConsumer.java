package io.eventuate.tram.testutil;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.util.test.async.Eventually;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.assertTrue;

public class TestMessageConsumer implements MessageHandler {

  private LinkedBlockingDeque<Message> messages = new LinkedBlockingDeque<>();

  private Logger logger = LoggerFactory.getLogger(getClass());
  private String replyChannel;

  public TestMessageConsumer(String replyChannel) {
    this.replyChannel = replyChannel;
  }

  public String getReplyChannel() {
    return replyChannel;
  }

  @Override
  public void accept(Message message) {
    logger.debug("Got message: {}", message);
    messages.add(message);
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
}
