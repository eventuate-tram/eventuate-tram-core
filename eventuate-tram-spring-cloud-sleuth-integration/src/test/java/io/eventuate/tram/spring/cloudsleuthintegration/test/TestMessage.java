package io.eventuate.tram.spring.cloudsleuthintegration.test;

public class TestMessage {

  private int port;

  public TestMessage() {
  }

  public TestMessage(int port) {

    this.port = port;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
