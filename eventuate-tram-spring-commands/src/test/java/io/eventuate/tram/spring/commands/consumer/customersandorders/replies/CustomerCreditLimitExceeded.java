package io.eventuate.tram.spring.commands.consumer.customersandorders.replies;

import io.eventuate.tram.commands.consumer.annotations.FailureReply;

@FailureReply
public class CustomerCreditLimitExceeded implements ReserveCreditReply {
}
