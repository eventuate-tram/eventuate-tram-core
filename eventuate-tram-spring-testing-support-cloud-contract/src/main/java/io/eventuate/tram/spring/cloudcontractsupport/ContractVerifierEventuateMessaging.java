package io.eventuate.tram.spring.cloudcontractsupport;

import io.eventuate.tram.messaging.common.Message;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifier;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import java.util.HashMap;

public class ContractVerifierEventuateMessaging extends ContractVerifierMessaging<Message> {
  public ContractVerifierEventuateMessaging(MessageVerifier<Message> exchange) {
    super(exchange);
  }

  @Override
  protected ContractVerifierMessage convert(Message receive) {
    if (receive == null)
      return null;
    return new ContractVerifierMessage(receive.getPayload(), new HashMap<>(receive.getHeaders()));
  }
}
