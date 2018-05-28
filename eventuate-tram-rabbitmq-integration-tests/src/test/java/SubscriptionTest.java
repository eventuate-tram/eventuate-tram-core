import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.consumer.rabbitmq.*;
import io.eventuate.tram.data.producer.rabbitmq.EventuateRabbitMQProducer;
import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.util.test.async.Eventually;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SubscriptionTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubscriptionTest {

  @Configuration
  @EnableAutoConfiguration
  public static class Config {
    @Bean
    public EventuateRabbitMQProducer rabbitMQMessageProducer(@Value("${rabbitmq.url}") String rabbitMQURL) {
      return new EventuateRabbitMQProducer(rabbitMQURL,
              Collections.emptyMap());
    }
  }


  @Value("${rabbitmq.url}")
  private String rabbitMQURL;

  @Value("${eventuatelocal.zookeeper.connection.string}")
  private String zkUrl;

  @Autowired
  private EventuateRabbitMQProducer eventuateRabbitMQProducer;

  @Test
  public void leaderAssignedThenRevoked() throws Exception {
    int messages = 100;
    String destination = "destination" + UUID.randomUUID();
    String subscriberId = "subscriber" + UUID.randomUUID();

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitMQURL);
    Connection connection = factory.newConnection();

    //created subscribtion selected as leader, assigned all partition to it self
    CoordinationCallbacks coordinationCallbacks = createSubscription(connection, subscriberId, destination, concurrentLinkedQueue);
    coordinationCallbacks.getLeaderSelectedCallback().run();
    coordinationCallbacks.getAssignmentUpdatedCallback().accept(new Assignment(ImmutableSet.of(destination), ImmutableMap.of(destination, ImmutableSet.of(0, 1))));

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    //check that all sent messages received by subscription
    Eventually.eventually(30, 1, TimeUnit.SECONDS, () -> Assert.assertEquals(messages, concurrentLinkedQueue.size()));

    //removing leadership, resigning all partitions
    coordinationCallbacks.getAssignmentUpdatedCallback().accept(new Assignment(ImmutableSet.of(destination), ImmutableMap.of(destination, ImmutableSet.of())));
    coordinationCallbacks.getLeaderRemovedCallback().run();
    concurrentLinkedQueue.clear();

    //new leader which assigns partitions to itself
    coordinationCallbacks = createSubscription(connection, subscriberId, destination, concurrentLinkedQueue);
    coordinationCallbacks.getLeaderSelectedCallback().run();
    coordinationCallbacks.getAssignmentUpdatedCallback().accept(new Assignment(ImmutableSet.of(destination), ImmutableMap.of(destination, ImmutableSet.of(0, 1))));

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(30, 1, TimeUnit.SECONDS, () -> Assert.assertEquals(messages, concurrentLinkedQueue.size()));

    connection.close();
  }

  @Test
  public void testParallelHandling() throws Exception {
    //2 partitions split between 2 subscriptions

    int messages = 100;
    String destination = "destination" + UUID.randomUUID();
    String subscriberId = "subscriber" + UUID.randomUUID();

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue1 = new ConcurrentLinkedQueue<>();

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitMQURL);
    Connection connection = factory.newConnection();


    CoordinationCallbacks coordinationCallbacks = createSubscription(connection, subscriberId, destination, concurrentLinkedQueue1);
    coordinationCallbacks.getLeaderSelectedCallback().run();
    coordinationCallbacks.getAssignmentUpdatedCallback().accept(new Assignment(ImmutableSet.of(destination), ImmutableMap.of(destination, ImmutableSet.of(0))));

    ConcurrentLinkedQueue<Integer> concurrentLinkedQueue2 = new ConcurrentLinkedQueue<>();

    coordinationCallbacks = createSubscription(connection, subscriberId, destination, concurrentLinkedQueue2);
    coordinationCallbacks.getAssignmentUpdatedCallback().accept(new Assignment(ImmutableSet.of(destination), ImmutableMap.of(destination, ImmutableSet.of(1))));

    for (int i = 0; i < messages; i++) {
      eventuateRabbitMQProducer.send(destination,
              String.valueOf(Math.random()),
              JSonMapper.toJson(new MessageImpl(String.valueOf(i),
                      Collections.singletonMap("ID", UUID.randomUUID().toString()))));
    }

    Eventually.eventually(30, 1, TimeUnit.SECONDS, () -> {
      Assert.assertFalse(concurrentLinkedQueue1.isEmpty());
      Assert.assertFalse(concurrentLinkedQueue2.isEmpty());
      Assert.assertEquals(messages, concurrentLinkedQueue1.size() + concurrentLinkedQueue2.size());
    });

    connection.close();
  }

  private CoordinationCallbacks createSubscription(Connection connection, String subscriberId, String destination, ConcurrentLinkedQueue concurrentLinkedQueue) {
    CoordinationCallbacks coordinationCallbacks = new CoordinationCallbacks();

    new Subscription(connection, zkUrl, subscriberId, ImmutableSet.of(destination), 2, (message, runnable) -> {
      concurrentLinkedQueue.add(Integer.valueOf(message.getPayload()));
    }) {
      @Override
      protected Coordinator createCoordinator(String groupMemberId,
                                              String zkUrl,
                                              String subscriberId,
                                              Set<String> channels,
                                              Runnable leaderSelectedCallback,
                                              Runnable leaderRemovedCallback,
                                              Consumer<Assignment> assignmentUpdatedCallback,
                                              Consumer<Map<String, Assignment>> manageAssignmentsCallback) {

        coordinationCallbacks.setLeaderSelectedCallback(leaderSelectedCallback);
        coordinationCallbacks.setLeaderRemovedCallback(leaderRemovedCallback);
        coordinationCallbacks.setAssignmentUpdatedCallback(assignmentUpdatedCallback);

        return Mockito.mock(Coordinator.class);
      }

      @Override
      protected PartitionManager createPartitionManager(int partitionCount) {
        return Mockito.mock(PartitionManager.class);
      }
    };

    return coordinationCallbacks;
  }

  private static class CoordinationCallbacks {
    private Runnable leaderSelectedCallback;
    private Consumer<Assignment> assignmentUpdatedCallback;
    private Runnable leaderRemovedCallback;

    public CoordinationCallbacks() {
    }

    public CoordinationCallbacks(Runnable leaderSelectedCallback, Consumer<Assignment> assignmentUpdatedCallback, Runnable leaderRemovedCallback) {
      this.leaderSelectedCallback = leaderSelectedCallback;
      this.assignmentUpdatedCallback = assignmentUpdatedCallback;
      this.leaderRemovedCallback = leaderRemovedCallback;
    }

    public Runnable getLeaderSelectedCallback() {
      return leaderSelectedCallback;
    }

    public void setLeaderSelectedCallback(Runnable leaderSelectedCallback) {
      this.leaderSelectedCallback = leaderSelectedCallback;
    }

    public Consumer<Assignment> getAssignmentUpdatedCallback() {
      return assignmentUpdatedCallback;
    }

    public void setAssignmentUpdatedCallback(Consumer<Assignment> assignmentUpdatedCallback) {
      this.assignmentUpdatedCallback = assignmentUpdatedCallback;
    }

    public Runnable getLeaderRemovedCallback() {
      return leaderRemovedCallback;
    }

    public void setLeaderRemovedCallback(Runnable leaderRemovedCallback) {
      this.leaderRemovedCallback = leaderRemovedCallback;
    }
  }
}
