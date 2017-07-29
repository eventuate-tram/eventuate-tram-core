package io.eventuate.tram.commands.consumer;

import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CommandDispatcherTest {

  static class CommandDispatcherTestTarget {

    @CommandHandlerMethod(path="/customers/{customerId}", replyChannel = "'CustomerAggregate'", partitionId="path['id']")
    public Success reserveCredit(@PathVariable("customerId") String customerId, CommandMessage<TestCommand> cm) {

      System.out.println("customerId=" + customerId);
      System.out.println("cm=" + cm);
      return new Success();

    }

  }

  static class TestCommand implements Command {
    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

  }

  @Test
  public void shouldDispatchCommand() {
    String commandDispatcherId = "fooId";
    String commandChannel = "commandChannel";

    CommandDispatcherTestTarget spy = spy(new CommandDispatcherTestTarget());

    ChannelMapping channelMapping = mock(ChannelMapping.class);

    MessageConsumer messageConsumer = mock(MessageConsumer.class);

    MessageProducer messageProducer = mock(MessageProducer.class);

    CommandDispatcher dispatcher = new CommandDispatcher(commandDispatcherId, spy, commandChannel,
            channelMapping,
            messageConsumer,
            messageProducer);

    String customerId = "customer0";
    String resource = "/customers/" + customerId;
    Command command = new TestCommand();

    Message message = CommandProducerImpl.makeMessage(resource, command, singletonMap(Message.ID, "999"));

    dispatcher.messageHandler(message);

    verify(spy).reserveCredit(eq(customerId), any(CommandMessage.class));
    verify(messageProducer).send(any(), any());
    verifyNoMoreInteractions(messageProducer, spy);
  }
}