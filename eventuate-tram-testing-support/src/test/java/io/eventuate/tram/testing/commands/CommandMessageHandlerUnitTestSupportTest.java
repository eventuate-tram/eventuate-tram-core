package io.eventuate.tram.testing.commands;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandHandlersBuilder;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import org.junit.Test;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;
import static io.eventuate.tram.testing.commands.CommandMessageHandlerUnitTestSupport.assertReplyTypeEquals;
import static io.eventuate.tram.testing.commands.CommandMessageHandlerUnitTestSupport.given;
import static org.mockito.Mockito.*;

public class CommandMessageHandlerUnitTestSupportTest {

  @Test
  public void shouldTest() {

    MyRepository myRepository = mock(MyRepository.class);

    MyCommandHandlers myCommandHandlers = spy(new MyCommandHandlers(myRepository));

    CommandHandlers commandHandlers = CommandHandlersBuilder
            .fromChannel("myCommandChannel")
            .onMessage(MyCommand.class, myCommandHandlers::handleMyCommand)
            .build();

    given()
            .commandHandlers(commandHandlers)
            .when()
            .receives(new MyCommand())
            .then()
            .expectCommandHandlerInvoked(myCommandHandlers, MyCommandHandlers::handleMyCommand, (CommandMessage<MyCommand> cm, CommandHandlerReply<Success> chr) -> {
                assertReplyTypeEquals(Success.class, chr.getReplyMessage());

                verify(myRepository).updateSomething();
            })
    ;
  }

  static class MyCommandHandlers {
    private MyRepository myRepository;

    public MyCommandHandlers(MyRepository myRepository) {
      this.myRepository = myRepository;
    }

    Message handleMyCommand(CommandMessage<MyCommand> c) {
      myRepository.updateSomething();
      return withSuccess();
    }
  }

  interface MyRepository {
    void updateSomething();
  }

  static class MyCommand implements Command {
  }
}
