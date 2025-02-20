package io.eventuate.tram.spring.commands.consumer.customersandorders;


import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.annotations.EventuateCommandHandler;
import io.eventuate.tram.spring.commands.consumer.customersandorders.commands.ReserveCreditCommand;
import io.eventuate.tram.spring.commands.consumer.customersandorders.replies.CustomerCreditLimitExceeded;
import io.eventuate.tram.spring.commands.consumer.customersandorders.replies.CustomerCreditReserved;
import io.eventuate.tram.spring.commands.consumer.customersandorders.replies.CustomerNotFound;
import io.eventuate.tram.spring.commands.consumer.customersandorders.replies.ReserveCreditReply;


public class CustomerCommandHandler {

  private final CustomerService customerService;

  public CustomerCommandHandler(CustomerService customerService) {
    this.customerService = customerService;
  }

  @EventuateCommandHandler(subscriberId="customerCommandDispatcher", channel="customerService")
  public ReserveCreditReply reserveCredit(CommandMessage<ReserveCreditCommand> cm) {
    ReserveCreditCommand cmd = cm.getCommand();
    try {
      customerService.reserveCredit(cmd.getCustomerId(), cmd.getOrderId(), cmd.getOrderTotal());
      return new CustomerCreditReserved();
    } catch (CustomerNotFoundException e) {
      return new CustomerNotFound();
    } catch (CustomerCreditLimitExceededException e) {
      return new CustomerCreditLimitExceeded();
    }
  }



}
