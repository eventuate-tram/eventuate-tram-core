package io.eventuate.tram.reactive.integrationtests.events;

import io.eventuate.tram.events.common.DomainEvent;

public class TestEventThatInitiatesException implements DomainEvent {

  private String payload;

  public TestEventThatInitiatesException() {
  }

  public TestEventThatInitiatesException(String payload) {
    this.payload = payload;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }
}
