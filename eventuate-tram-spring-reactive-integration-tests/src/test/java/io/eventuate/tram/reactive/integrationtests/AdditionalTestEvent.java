package io.eventuate.tram.reactive.integrationtests;

import io.eventuate.tram.events.common.DomainEvent;

public class AdditionalTestEvent implements DomainEvent {

  private String payload;

  public AdditionalTestEvent() {
  }

  public AdditionalTestEvent(String payload) {
    this.payload = payload;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }
}
