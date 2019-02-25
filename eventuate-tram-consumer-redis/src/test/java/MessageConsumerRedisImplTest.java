import io.eventuate.tram.consumer.redis.MessageConsumerRedisImpl;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.redis.common.CommonRedisConfiguration;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommonRedisConfiguration.class)
public class MessageConsumerRedisImplTest {

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Test
  public void testMessageReceived() {
    TestInfo testInfo = new TestInfo();

    MessageConsumer messageConsumer = createMessageConsumer(true);

    List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), messages::add);

    sendMessage(testInfo.getKey(), testInfo.getMessage(), testInfo.getMessageId(), testInfo.getChannel());

    waitForMessage(messages, testInfo.getMessage());
  }

  @Test
  public void testMessageReceivedWhenConsumerGroupExists() throws Exception {
    TestInfo testInfo = new TestInfo();

    sendMessage(testInfo.getKey(), testInfo.getMessage(), testInfo.getMessageId(), testInfo.getChannel());

    redisTemplate.opsForStream().createGroup(testInfo.getChannel(), ReadOffset.from("0"), testInfo.getSubscriberId());

    MessageConsumer messageConsumer = createMessageConsumer(true);

    List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), messages::add);

    waitForMessage(messages, testInfo.getMessage());
  }

  @Test
  public void testReceivingPendingMessageAfterRestart() throws InterruptedException {
    TestInfo testInfo = new TestInfo();

    MessageConsumerRedisImpl messageConsumer = createMessageConsumer(false);

    List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), message -> {
      messages.add(message);
      throw new RuntimeException("Something happened!");
    });

    sendMessage(testInfo.getKey(), testInfo.getMessage(), testInfo.getMessageId(), testInfo.getChannel());

    Eventually.eventually(() -> {
      Assert.assertEquals(1, messages.size());
      Assert.assertEquals(testInfo.getMessage(), messages.get(0).getPayload());
    });

    messages.clear();

    messageConsumer.close();

    messageConsumer = createMessageConsumer(false);

    messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), messages::add);

    waitForMessage(messages, testInfo.getMessage());
  }

  @Test
  public void testMessageThatExceptionInMessageConsumerIsHandled() {
    TestInfo testInfo = new TestInfo();

    MessageConsumerRedisImpl messageConsumer = createMessageConsumer(true);

    List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), message -> {
      if (messages.isEmpty()) {
        messages.add(message);
        throw new RuntimeException("Something happened!");
      }
      messages.add(message);
    });

    sendMessage(testInfo.getKey(), testInfo.getMessage(), testInfo.getMessageId(), testInfo.getChannel());
    sendMessage(testInfo.getKey(), UUID.randomUUID().toString(), UUID.randomUUID().toString(), testInfo.getChannel());

    Eventually.eventually(() -> {
      Assert.assertEquals(2, messages.size());
      Assert.assertEquals(testInfo.getMessage(), messages.get(0).getPayload());
    });
  }

  private void waitForMessage(List<Message> messages, String message) {
    Eventually.eventually(() -> {
      Assert.assertEquals(1, messages.size());
      Assert.assertEquals(message, messages.get(0).getPayload());
    });
  }


  private MessageConsumerRedisImpl createMessageConsumer(boolean acknowledgeFailedMessages) {
    MessageConsumerRedisImpl messageConsumer = new MessageConsumerRedisImpl(redisTemplate, acknowledgeFailedMessages);

    messageConsumer.setDuplicateMessageDetector((consumerId, messageId) -> false);
    messageConsumer.setTransactionTemplate(new TransactionTemplate() {
      @Override
      public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        return action.doInTransaction(null);
      }
    });

    return messageConsumer;
  }

  private void sendMessage(String key, String message, String messageId, String channel) {
    redisTemplate
            .opsForStream()
            .add(StreamRecords.string(Collections.singletonMap(key,
                    String.format("{\"payload\": \"%s\", \"headers\" : {\"ID\" : \"%s\"}}",
                            message,
                            messageId))).withStreamKey(channel));
  }


  private static class TestInfo {
    private String subscriberId = UUID.randomUUID().toString();
    private String channel = UUID.randomUUID().toString();
    private String key = UUID.randomUUID().toString();
    private String message = UUID.randomUUID().toString();
    private String messageId = UUID.randomUUID().toString();

    public String getSubscriberId() {
      return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
      this.subscriberId = subscriberId;
    }

    public String getChannel() {
      return channel;
    }

    public void setChannel(String channel) {
      this.channel = channel;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public String getMessageId() {
      return messageId;
    }

    public void setMessageId(String messageId) {
      this.messageId = messageId;
    }
  }
}
