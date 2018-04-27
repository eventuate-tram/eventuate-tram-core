package io.eventuate.e2e.tests.basic.events;

import io.eventuate.e2e.tests.basic.events.domain.Account;

public class AbstractTramEventTestConfig {

  private long uniqueId = System.currentTimeMillis();
  private String  aggregateType = Account.class.getName() + uniqueId;
  private String aggregateId = "accountId" + uniqueId;

  public long getUniqueId() {
    return uniqueId;
  }

  public String getAggregateType() {
    return aggregateType;
  }

  public String getAggregateId() {
    return aggregateId;
  }
}
