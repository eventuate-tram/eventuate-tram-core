package io.eventuate.tram.commands.db.broker.integrationtests;

import io.eventuate.tram.commands.CommandDestination;
import io.eventuate.tram.commands.common.Command;

@CommandDestination("destination")
public class MyTestCommand implements Command {

  private String name;

  public MyTestCommand() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
