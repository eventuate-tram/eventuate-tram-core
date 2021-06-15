package io.eventuate.tram.reactive.integrationtests.commands;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandDispatcher;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandDispatcherFactory;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducer;
import io.eventuate.tram.reactive.integrationtests.IdSupplier;
import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import io.eventuate.tram.spring.reactive.commands.consumer.ReactiveTramCommandConsumerConfiguration;
import io.eventuate.tram.spring.reactive.commands.producer.ReactiveTramCommandProducerConfiguration;
import io.eventuate.tram.spring.reactive.consumer.common.ReactiveTramConsumerCommonConfiguration;
import io.eventuate.tram.spring.reactive.consumer.kafka.EventuateTramReactiveKafkaMessageConsumerConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTramCommandIntegrationTest.Config.class)
public class ReactiveTramCommandIntegrationTest {

  @Import({ReactiveTramMessageProducerJdbcConfiguration.class,
          ReactiveTramConsumerCommonConfiguration.class,
          EventuateTramReactiveKafkaMessageConsumerConfiguration.class,
          ReactiveTramCommandConsumerConfiguration.class,
          ReactiveTramCommandProducerConfiguration.class})
  @Configuration
  @EnableAutoConfiguration
  public static class Config {

    @Bean
    public ReactiveTramTestCommandHandler reactiveTramTestCommandHandler() {
      return new ReactiveTramTestCommandHandler(IdSupplier.get());
    }

    @Bean
    public ReactiveCommandDispatcher commandDispatcher(ReactiveCommandDispatcherFactory commandDispatcherFactory,
                                                       ReactiveTramTestCommandHandler reactiveTramTestCommandHandler) {
      return commandDispatcherFactory.make(IdSupplier.get(), reactiveTramTestCommandHandler.getCommandHandlers());
    }
  }

  @Autowired
  private ReactiveCommandProducer commandProducer;

  @Autowired
  private ReactiveMessageConsumer messageConsumer;

  @Autowired
  private ReactiveTramTestCommandHandler reactiveTramTestCommandHandler;

  private BlockingQueue<Message> messageQueue = new LinkedBlockingDeque<>();

  private String payload = IdSupplier.get();
  private String replyChannel = IdSupplier.get();
  private String subscriberId = IdSupplier.get();

  @Test
  public void shouldInvokeCommand() throws InterruptedException {
    subscribeToReplyChannel();

    String commandId = sendCommand();

    assertCommandReceived();

    assertReplyReceived(commandId);
  }

  private void assertCommandReceived() throws InterruptedException {
    TestCommand testCommand = reactiveTramTestCommandHandler.getCommandQueue().poll(10, TimeUnit.SECONDS);
    assertEquals(payload, testCommand.getPayload());
  }

  private void assertReplyReceived(String commandId) throws InterruptedException {
    Message m = messageQueue.poll(10, TimeUnit.SECONDS);
    assertEquals(commandId, m.getRequiredHeader(ReplyMessageHeaders.IN_REPLY_TO));
  }

  private String sendCommand() {
    return commandProducer
            .send(reactiveTramTestCommandHandler.getCommandChannel(),
                    new TestCommand(payload),
                    replyChannel,
                    Collections.emptyMap())
            .block();
  }

  private void subscribeToReplyChannel() {
    messageConsumer.subscribe(subscriberId, Collections.singleton(replyChannel), this::handleMessage);
  }

  private Publisher<?> handleMessage(Message message) {
    messageQueue.add(message);

    return Mono.empty();
  }
}
