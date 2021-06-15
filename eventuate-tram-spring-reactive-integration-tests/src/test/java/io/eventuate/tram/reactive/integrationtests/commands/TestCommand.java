package io.eventuate.tram.reactive.integrationtests.commands;

import io.eventuate.tram.commands.common.Command;

public class TestCommand implements Command {

  private String payload;

  public TestCommand() {
  }

  public TestCommand(String payload) {
    this.payload = payload;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }
}
