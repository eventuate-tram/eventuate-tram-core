package io.eventuate.tram.reactive.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlersBuilder;
import io.eventuate.util.test.async.Eventually;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@MockitoSettings(strictness = Strictness.WARN)
@ExtendWith(MockitoExtension.class)
public class ReactiveNotificationTests extends ReactiveAbstractCommandDispatchingTests {

  @Spy
  private CommandDispatcherTestTarget target = new CommandDispatcherTestTarget();

  static class TestNotification implements Command {
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

  }

  static class CommandDispatcherTestTarget {


    public Mono<Void> handleNotification(CommandMessage<TestNotification> cm) {
      return null;
    }

  }

  public ReactiveCommandHandlers defineCommandHandlers() {
    return ReactiveCommandHandlersBuilder
            .fromChannel(channel)
            .onNotification(TestNotification.class, target::handleNotification)
            .build();
  }


  @Test
  public void testSendingNotification() {

    String messageId = commandProducer.sendNotification(channel, new TestNotification(), Collections.emptyMap()).block();
    assertNotNull(messageId);

    Eventually.eventually(() -> {
      verify(target).handleNotification(any(CommandMessage.class));
      verifyNoMoreInteractions(target);

    });

  }
}
