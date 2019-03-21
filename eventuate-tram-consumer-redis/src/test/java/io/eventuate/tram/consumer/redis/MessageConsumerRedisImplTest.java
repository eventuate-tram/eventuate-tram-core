package io.eventuate.tram.consumer.redis;

import io.eventuate.tram.consumer.common.TramNoopDuplicateMessageDetectorConfiguration;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageSubscription;
import io.eventuate.tram.redis.common.RedisConfigurationProperties;
import io.eventuate.tram.redis.common.RedisUtil;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TramConsumerRedisConfiguration.class, TramNoopDuplicateMessageDetectorConfiguration.class})
public class MessageConsumerRedisImplTest {

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private RedisConfigurationProperties redisConfigurationProperties;

  @Autowired
  private MessageConsumerRedisImpl messageConsumer;

  @Test
  public void testMessageReceived() {
    TestInfo testInfo = new TestInfo();

    List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), messages::add);

    sendMessage(testInfo.getKey(), testInfo.getMessage(), testInfo.getMessageId(), testInfo.getChannel());

    waitForMessage(messages, testInfo.getMessage());
  }

  @Test
  public void testMessageReceivedWhenConsumerGroupExists() {
    TestInfo testInfo = new TestInfo();

    for (int i = 0; i < redisConfigurationProperties.getPartitions(); i++) {
      sendMessage(testInfo.getKey(), testInfo.getMessage(), testInfo.getMessageId(), testInfo.getChannel(), i);
      redisTemplate.opsForStream().createGroup(RedisUtil.channelToRedisStream(testInfo.getChannel(), i), ReadOffset.from("0"), testInfo.getSubscriberId());
    }

    List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), messages::add);

    Eventually.eventually(() -> {
      Assert.assertEquals(redisConfigurationProperties.getPartitions(), messages.size());
      Assert.assertEquals(testInfo.getMessage(), messages.get(0).getPayload());
      Assert.assertEquals(testInfo.getMessage(), messages.get(1).getPayload());
    });
  }

  @Test
  public void testReceivingPendingMessageAfterRestart() {
    TestInfo testInfo = new TestInfo();

    List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    MessageSubscription messageSubscription = messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), message -> {
      messages.add(message);
      throw new RuntimeException("Something happened!");
    });

    sendMessage(testInfo.getKey(), testInfo.getMessage(), testInfo.getMessageId(), testInfo.getChannel());

    Eventually.eventually(() -> {
      Assert.assertEquals(1, messages.size());
      Assert.assertEquals(testInfo.getMessage(), messages.get(0).getPayload());
    });

    messages.clear();

    messageSubscription.unsubscribe();

    messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), messages::add);

    waitForMessage(messages, testInfo.getMessage());
  }

  @Test
  public void testThatProcessingStoppedOnException() {
    TestInfo testInfo = new TestInfo();

    List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    messageConsumer.subscribe(testInfo.getSubscriberId(), Collections.singleton(testInfo.getChannel()), message -> {
      if (messages.isEmpty()) {
        messages.add(message);
        throw new RuntimeException("Something happened!");
      }
      messages.add(message);
    });

    sendMessage(testInfo.getKey(), testInfo.getMessage(), testInfo.getMessageId(), testInfo.getChannel());

    Eventually.eventually(() -> {
      Assert.assertEquals(1, messages.size());
    });

    messageConsumer.close();
  }

  private void waitForMessage(List<Message> messages, String message) {
    Eventually.eventually(60, 500, TimeUnit.MILLISECONDS,() -> {
      Assert.assertEquals(1, messages.size());
      Assert.assertEquals(message, messages.get(0).getPayload());
    });
  }

  private void sendMessage(String key, String message, String messageId, String channel) {
    int partition = Math.abs(key.hashCode()) % redisConfigurationProperties.getPartitions();

    sendMessage(key, message, messageId, channel, partition);
  }

  private void sendMessage(String key, String message, String messageId, String channel, int partition) {
    redisTemplate
            .opsForStream()
            .add(StreamRecords.string(Collections.singletonMap(key,
                    String.format("{\"payload\": \"%s\", \"headers\" : {\"ID\" : \"%s\"}}",
                            message,
                            messageId))).withStreamKey(RedisUtil.channelToRedisStream(channel, partition)));
  }


  private static class TestInfo {
    private String subscriberId = "subscriber" + System.nanoTime();
    private String channel = "channel" + System.nanoTime();

    private String key = "key1";
    private String message = "message1";
    private String messageId = "msg1";

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
