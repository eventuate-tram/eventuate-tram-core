package io.eventuate.tram.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandHandlersBuilder;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.util.test.async.Eventually;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@MockitoSettings(strictness = Strictness.WARN)
@ExtendWith(MockitoExtension.class)
public class NotificationTests extends AbstractCommandDispatchingTests {


  static class TestNotification implements Command {
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

  }

  @Spy
  protected NotificationTests.CommandDispatcherTestTarget target = new NotificationTests.CommandDispatcherTestTarget();

  static class CommandDispatcherTestTarget {


    public void handleNotification(CommandMessage<TestNotification> cm) {
    }

  }

  @Override
  public CommandHandlers defineCommandHandlers() {
    return CommandHandlersBuilder
            .fromChannel(channel)
            .onMessage(NotificationTests.TestNotification.class, target::handleNotification)
            .build();
  }

  @Test
  public void testSendingNotification() {

    String messageId = inMemoryCommands.commandProducer.sendNotification(channel, new TestNotification(), Collections.emptyMap());
    assertNotNull(messageId);

    Eventually.eventually(() -> {
      verify(target).handleNotification(any(CommandMessage.class));
      verifyNoMoreInteractions(target);

    });

  }
}
