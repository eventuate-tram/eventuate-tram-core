package io.eventuate.tram.commands.consumer;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static io.eventuate.tram.commands.producer.CommandMessageFactory.makeMessage;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommandDispatcherTest {

  @Spy
  private CommandDispatcherTestTarget target = new CommandDispatcherTestTarget();

  @Mock
  private MessageConsumer messageConsumer;

  @Mock
  private MessageProducer messageProducer;

  @Mock
  private CommandNameMapping commandNameMapping;
  private CommandDispatcher dispatcher;


  static class CommandDispatcherTestTarget {


    public Message reserveCredit(CommandMessage<TestCommand> cm) {
      return MessageBuilder
              .withPayload(JSonMapper.toJson(new Success()))
              .build();
    }

    public void handleNotification(CommandMessage<TestNotification> cm) {
    }

  }

  static class TestCommand implements Command {
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

  }

  static class TestNotification implements Command {
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

  }

  public CommandHandlers defineCommandHandlers(CommandDispatcherTestTarget target) {
    return CommandHandlersBuilder
            .fromChannel("customerService")
            .onMessage(TestCommand.class, target::reserveCredit)
            .onMessage(TestNotification.class, target::handleNotification)
            .build();
  }

  private String commandDispatcherId = "fooId";
  private String externalCommandName = "extTestCommand";
  private String externalNotificationName = "extTestNotification";

  private String replyTo = "replyTo-xxx";

  private String channel = "myChannel";

  @Before
  public void setup() {
    when(commandNameMapping.commandToExternalCommandType(any(TestCommand.class))).thenReturn(externalCommandName);
    when(commandNameMapping.externalCommandTypeToCommandClassName(externalCommandName)).thenReturn(TestCommand.class.getName());

    when(commandNameMapping.commandToExternalCommandType(any(TestNotification.class))).thenReturn(externalNotificationName);
    when(commandNameMapping.externalCommandTypeToCommandClassName(externalNotificationName)).thenReturn(TestNotification.class.getName());

    dispatcher = new CommandDispatcher(commandDispatcherId,
            defineCommandHandlers(target),
            messageConsumer,
            commandNameMapping, new CommandReplyProducer(messageProducer));

  }

  @Test
  public void shouldDispatchCommand() {

    Command command = new TestCommand();

    Message message = makeMessage(commandNameMapping, channel, command, replyTo, singletonMap(Message.ID, "999"));

    dispatcher.messageHandler(message);

    verify(target).reserveCredit(any(CommandMessage.class));
    verify(messageProducer).send(any(), any());
    verifyNoMoreInteractions(messageProducer, target);

    verify(commandNameMapping).commandToExternalCommandType(command);
    verify(commandNameMapping).externalCommandTypeToCommandClassName(externalCommandName);
    verifyNoMoreInteractions(commandNameMapping);
  }

  @Test
  public void shouldDispatchNotification() {

    Command notification = new TestNotification();

    Message message = makeMessage(commandNameMapping, channel, notification, null, singletonMap(Message.ID, "999"));

    dispatcher.messageHandler(message);

    verify(target).handleNotification(any(CommandMessage.class));
    verifyNoMoreInteractions(messageProducer, target);

    verify(commandNameMapping).commandToExternalCommandType(notification);
    verify(commandNameMapping).externalCommandTypeToCommandClassName(externalNotificationName);
    verifyNoMoreInteractions(commandNameMapping);
  }
}