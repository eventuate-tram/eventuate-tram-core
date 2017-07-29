package io.eventuate.tram.commandsandevents.integrationtests;

import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.commands.consumer.CommandHandler;
import io.eventuate.tram.commands.consumer.CommandHandlerMethod;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariable;

@CommandHandler("customerService")
public class MyTestCommandHandler {

  @CommandHandlerMethod(path="/customers/{customerId}", replyChannel = "'CustomerAggregate'", partitionId="path['id']")
  public Success myHandlerMethod(@PathVariable("customerId") String customerId, CommandMessage<MyTestCommand> cm) {
    System.out.println("Got command: " + cm);
    return new Success();
  }
}
