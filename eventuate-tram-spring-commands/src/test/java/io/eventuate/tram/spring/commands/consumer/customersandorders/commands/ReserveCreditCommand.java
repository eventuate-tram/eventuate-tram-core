package io.eventuate.tram.spring.commands.consumer.customersandorders.commands;

import io.eventuate.examples.common.money.Money;
import io.eventuate.tram.commands.common.Command;

public final class ReserveCreditCommand  implements Command {
  private Long customerId;
  private Long orderId;
  private Money orderTotal;

  public ReserveCreditCommand() {
  }

  public ReserveCreditCommand(Long customerId, Long orderId, Money orderTotal) {
    this.customerId = customerId;
    this.orderId = orderId;
    this.orderTotal = orderTotal;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long customerId) {
    this.customerId = customerId;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Money orderTotal) {
    this.orderTotal = orderTotal;
  }
}