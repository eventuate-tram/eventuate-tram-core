package io.eventuate.tram.commands.consumer;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CommandDispatcherTest {

  static class CommandDispatcherTestTarget {


    public Message reserveCredit(CommandMessage<TestCommand> cm, PathVariables pathVariables) {

      String customerId = pathVariables.getString("customerId");
      System.out.println("customerId=" + customerId);
      System.out.println("cm=" + cm);
      return MessageBuilder
              .withPayload(JSonMapper.toJson(new Success()))
              .build();

    }

  }

  static class TestCommand implements Command {
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

  }

  public CommandHandlers defineCommandHandlers(CommandDispatcherTestTarget target) {
    return CommandHandlersBuilder
            .fromChannel("customerService")
            .resource("/customers/{customerId}")
            .onMessage(TestCommand.class, target::reserveCredit)
            .build();
  }

  @Test
  public void shouldDispatchCommand() {
    String commandDispatcherId = "fooId";

    CommandDispatcherTestTarget target = spy(new CommandDispatcherTestTarget());

    ChannelMapping channelMapping = mock(ChannelMapping.class);

    MessageConsumer messageConsumer = mock(MessageConsumer.class);

    MessageProducer messageProducer = mock(MessageProducer.class);

    CommandDispatcher dispatcher = new CommandDispatcher(commandDispatcherId,
            defineCommandHandlers(target),
            channelMapping,
            messageConsumer,
            messageProducer);

    String customerId = "customer0";
    String resource = "/customers/" + customerId;
    Command command = new TestCommand();

    String replyTo = "replyTo-xxx";

    String channel = "myChannel";

    Message message = CommandProducerImpl.makeMessage(channel, resource, command, replyTo, singletonMap(Message.ID, "999"));

    dispatcher.messageHandler(message);

    verify(target).reserveCredit(any(CommandMessage.class), any(PathVariables.class));
    verify(messageProducer).send(any(), any());
    verifyNoMoreInteractions(messageProducer, target);
  }
}