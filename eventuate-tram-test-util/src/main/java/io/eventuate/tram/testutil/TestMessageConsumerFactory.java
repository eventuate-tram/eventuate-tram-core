package io.eventuate.tram.testutil;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

public class TestMessageConsumerFactory {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private MessageConsumer messageConsumer;


  public TestMessageConsumer make() {
    String replyChannel = "reply-channel-" + System.currentTimeMillis();
    String subscriberId = "subscriberId-" + System.currentTimeMillis();

    TestMessageConsumer consumer = new TestMessageConsumer(replyChannel);

    messageConsumer.subscribe(subscriberId, Collections.singleton(replyChannel), consumer);

    return consumer;

  }
}

