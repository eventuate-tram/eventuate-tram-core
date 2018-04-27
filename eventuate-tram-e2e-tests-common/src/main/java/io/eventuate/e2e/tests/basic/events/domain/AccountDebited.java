package io.eventuate.e2e.tests.basic.events.domain;

import io.eventuate.tram.events.common.DomainEvent;

public class AccountDebited implements DomainEvent {

  private long amount;

  public AccountDebited() {
  }

  public AccountDebited(long amount) {

    this.amount = amount;
  }

  public long getAmount() {
    return amount;
  }

  public void setAmount(long amount) {
    this.amount = amount;
  }
}
