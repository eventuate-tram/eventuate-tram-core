package io.eventuate.tram.spring.cloudcontractsupport;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.toomuchcoding.jsonassert.JsonAssertion;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.stubrunner.BatchStubRunner;
import org.springframework.cloud.contract.verifier.util.*;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EventuateTramRoutesConfigurer {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private BatchStubRunner batchStubRunner;

  public EventuateTramRoutesConfigurer(BatchStubRunner batchStubRunner) {
    this.batchStubRunner = batchStubRunner;
  }

  @Autowired
  private MessageConsumer messageConsumer;

  @Autowired
  private MessageProducer messageProducer;

  private int idCounter;

  @PostConstruct
  public void initialize() {
    for (Collection<Contract> contracts : batchStubRunner.getContracts().values()) {
      for (Contract contract : contracts) {
        if (isCommandMessageContract(contract)) {

          String commandChannel = contract.getInput().getMessageFrom().getClientValue();
          String replyToChannel = contract.getOutputMessage().getSentTo().getClientValue();

          messageConsumer.subscribe("Route-" + contract.getLabel() + System.currentTimeMillis() + "." + idCounter++,
                  Collections.singleton(commandChannel),
                  message -> {
                    if (satisfies(message, contract)) {
                      messageProducer.send(replyToChannel, makeReply(message, contract));
                    }
                  });
        }
      }
    }

  }

  private static boolean isCommandMessageContract(Contract contract) {
    return contract.getInput() != null
            && contract.getInput().getMessageFrom() != null
            && contract.getOutputMessage() != null
            && contract.getOutputMessage().getSentTo() != null;
  }

  private Message makeReply(Message message, Contract groovyDsl) {
    MessageBuilder messageBuilder = MessageBuilder
            .withPayload(BodyExtractor
                    .extractStubValueFrom(groovyDsl.getOutputMessage().getBody()));
    if (groovyDsl.getOutputMessage().getHeaders() != null) {
      for (Header entry : groovyDsl.getOutputMessage().getHeaders().getEntries()) {
        messageBuilder.withHeader(entry.getName(), entry.getClientValue().toString());
      }
    }
    messageBuilder.withExtraHeaders("", correlationHeaders(message.getHeaders()));

    return messageBuilder.build();
  }

  private Map<String, String> correlationHeaders(Map<String, String> headers) {
    Map<String, String> m = headers.entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith(CommandMessageHeaders.COMMAND_HEADER_PREFIX))
            .collect(Collectors.toMap(e -> CommandMessageHeaders.inReply(e.getKey()),
                    Map.Entry::getValue));
    m.put(ReplyMessageHeaders.IN_REPLY_TO, headers.get(Message.ID));
    return m;
  }

  private boolean satisfies(Message message, Contract groovyDsl) {
    if (!headersMatch(message, groovyDsl)) {
      logger.info("Headers don't match {} {} ", groovyDsl.getLabel(), message);
      return false;
    }
    return bodyMatches(message, groovyDsl);
  }

  private boolean bodyMatches(Message message, Contract groovyDsl) {
    DocumentContext parsedJson = JsonPath.parse(message.getPayload());

    BodyMatchers matchers = groovyDsl.getInput().getBodyMatchers();

    Object dslBody = MapConverter.getStubSideValues(groovyDsl.getInput().getMessageBody());
    Object matchingInputMessage = JsonToJsonPathsConverter.removeMatchingJsonPaths(dslBody, matchers);
    JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithStubsSideValuesAndNoArraySizeCheck(matchingInputMessage);

    boolean matches = true;
    for (MethodBufferingJsonVerifiable path : jsonPaths) {
      matches &= matchesJsonPath(parsedJson, path.jsonPath());
    }
    logger.info("jsonPaths match {} {} {} ", groovyDsl.getLabel(), matches, message);

    if (matches && matchers != null && matchers.hasMatchers()) {
      for (BodyMatcher matcher : matchers.matchers()) {
        String jsonPath = JsonToJsonPathsConverter.convertJsonPathAndRegexToAJsonPath(matcher, dslBody);
        matches &= matchesJsonPath(parsedJson, jsonPath);
      }
    }
    logger.info("matchers {} {} {} ", groovyDsl.getLabel(), matches, message);
    return matches;
  }

  private boolean matchesJsonPath(DocumentContext parsedJson, String jsonPath) {
    try {
      JsonAssertion.assertThat(parsedJson).matchesJsonPath(jsonPath);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean headersMatch(Message message, Contract groovyDsl) {
    Map<String, String> headers = message.getHeaders();
    boolean matches = true;
    for (Header contractHeader : groovyDsl.getInput().getMessageHeaders().getEntries()) {
      String name = contractHeader.getName();
      Object value = contractHeader.getClientValue();
      Object valueInHeader = headers.get(name);
      matches &= value instanceof Pattern ? ((Pattern) value).matcher(valueInHeader.toString()).matches() :
              valueInHeader != null && valueInHeader.equals(value);
      logger.info("matches {} name {} pattern? {} headerValue {} value {}", matches, name, value instanceof Pattern, valueInHeader, value);
    }
    return matches;
  }
}
