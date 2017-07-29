package io.eventuate.tram.commands.consumer;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.Failure;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.common.paths.ResourcePath;
import io.eventuate.tram.commands.common.paths.ResourcePathPattern;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;

public class CommandDispatcher {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private String commandDispatcherId;
  private final Object target;
  private final String commandChannel;
  private final ChannelMapping channelMapping;
  private final MessageConsumer messageConsumer;
  private MessageProducer messageProducer;

  public CommandDispatcher(String commandDispatcherId, Object target, String commandChannel,
                           ChannelMapping channelMapping,
                           MessageConsumer messageConsumer,
                           MessageProducer messageProducer) {
    this.commandDispatcherId = commandDispatcherId;
    this.target = target;
    this.commandChannel = commandChannel;
    this.channelMapping = channelMapping;
    this.messageConsumer = messageConsumer;
    this.messageProducer = messageProducer;
  }

  @PostConstruct
  public void initialize() {
    messageConsumer.subscribe(commandDispatcherId, singleton(commandChannel), this::messageHandler);
  }

  void messageHandler(Message message) {
    System.out.println("Received message=" + message);

    // Need to serialize and route message to handler
    // Insert reply into MESSAGES table

    Optional<Method> possibleMethod = findTargetMethod(message);
    if (!possibleMethod.isPresent()) {
      throw new RuntimeException("No method for " + message);
    }

    Method m = possibleMethod.get();

    String payload = message.getPayload();
    Class<?> paramType = findCommandParameterType(m);
    System.out.println("Payload=" + "s");
    System.out.println("Payload=" + payload);
    System.out.println("paramType=" + paramType);
    Object param = JSonMapper.fromJson(payload, paramType);

    Object reply;
    Map<String, String> correlationHeaders = correlationHeaders(message.getHeaders());

    ResourcePathPattern r = ResourcePathPattern.parse(m.getDeclaredAnnotation(CommandHandlerMethod.class).path());
    ResourcePath mr = ResourcePath.parse(message.getRequiredHeader(CommandMessageHeaders.RESOURCE));
    Map<String, String> pathVars = r.getPathVariableValues(mr);

    try {
      reply = m.invoke(target, makeCommandHandlerParameters(m, message, param, correlationHeaders));
    } catch (InvocationTargetException e) {
      logger.error("Exception invoking method", e.getCause());
      try {
        handleException(message, param, m, e.getCause(), pathVars);
        return;
      } catch (InvocationTargetException | IllegalAccessException e1) {
        throw new RuntimeException(e);
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    if (reply != null) {

      ReplyDestination replyDestination = destination(param, m, reply, pathVars);
      System.out.println("Sending reply event " + replyDestination);
      System.out.println("Publishing reply event with headers: " + correlationHeaders + " to " + replyDestination);
      publish(replyDestination, correlationHeaders, reply);
    }
  }

  private ExpressionParser parser = new SpelExpressionParser();

  public ReplyDestination destination(Object parameter, Method handlerMethod, Object result, Map<String, String> pathVars) {
    DestinationRootObject rootObject = new DestinationRootObject(parameter, result, pathVars);
    return evaluate(rootObject, handlerMethod.getAnnotation(CommandHandlerMethod.class));
  }

  private ReplyDestination evaluate(Object rootObject, CommandHandlerMethod da) {
    EvaluationContext ctx = new StandardEvaluationContext(rootObject);

    Expression aggregateTypeExpression = parser.parseExpression(da.replyChannel());
    Expression aggregateIdExpression = parser.parseExpression(da.partitionId());


    String originalDestination = (String) aggregateTypeExpression.getValue(ctx);
    String partitionKey = (String)aggregateIdExpression.getValue(ctx);

    return new ReplyDestination(channelMapping.transform(originalDestination), partitionKey);
  }

  public ReplyDestination destinationForException(Object parameter, Method handlerMethod, Throwable throwable, Object result, Map<String, String> pathVars) {
    DestinationRootObject rootObject = new DestinationRootObjectForException(parameter, result, pathVars, throwable);
    return evaluate(rootObject, handlerMethod.getAnnotation(CommandHandlerMethod.class));
  }

  private void publish(ReplyDestination replyDestination, Map<String, String> correlationHeaders, Object reply) {

    messageProducer.send(replyDestination.destination,
            MessageBuilder
                    .withPayload(JSonMapper.toJson(reply))
                    .withExtraHeaders("", correlationHeaders)
                    .withHeader(Message.PARTITION_ID, replyDestination.partitionKey)
                    .withHeader(ReplyMessageHeaders.REPLY_TYPE, reply.getClass().getName())
                    .build());
  }

  private Object[] makeCommandHandlerParameters(Method m, Message message, Object param, Map<String, String> correlationHeaders) {
    return Arrays.stream(m.getParameters()).map(p -> {
      if (CommandMessage.class.isAssignableFrom(p.getType()))
        return new CommandMessage(message.getId(), param, correlationHeaders);
      else {
        String name = p.getName();
        if (p.getAnnotation(PathVariable.class) != null) {
          ResourcePathPattern r = ResourcePathPattern.parse(m.getDeclaredAnnotation(CommandHandlerMethod.class).path());
          ResourcePath mr = ResourcePath.parse(message.getRequiredHeader(CommandMessageHeaders.RESOURCE));
          System.out.println("looking for name=" + p.getAnnotation(PathVariable.class).value());
          return r.getPathVariableValues(mr).get(p.getAnnotation(PathVariable.class).value());
        } else
          throw new UnsupportedOperationException("Dont know what to do with parameter: " + name);
      }
    }).collect(toList()).toArray();
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

  private void handleException(Message message, Object param, Method method, Throwable cause, Map<String, String> pathVars) throws InvocationTargetException, IllegalAccessException {
    Optional<Method> m = findExceptionHandler(cause);

    logger.info("Handler for {} is {}", cause.getClass(), m);


    if (m.isPresent()) {
      Object reply = m.get().invoke(target, cause);
      ReplyDestination replyDestination = destinationForException(param, method, cause, reply, pathVars);
      publish(replyDestination, correlationHeaders(message.getHeaders()), reply);
    } else {
      Failure reply = new Failure();
      ReplyDestination replyDestination = destinationForException(param, method, cause, reply, pathVars);
      publish(replyDestination, correlationHeaders(message.getHeaders()), reply);
    }
  }

  private Optional<Method> findTargetMethod(Message message) {
    Class<?> targetClass = target.getClass();
    while (targetClass != Object.class) {
      for (Method m : targetClass.getMethods()) {
        if (parameterTypeMatches(message, m) && resourceMatches(message.getRequiredHeader(CommandMessageHeaders.RESOURCE), m.getDeclaredAnnotation(CommandHandlerMethod.class).path())) {
          return Optional.of(m);
        }
      }
      targetClass = targetClass.getSuperclass();
    }
    return Optional.empty();
  }

  private boolean resourceMatches(String messageResource, String methodPath) {
    ResourcePathPattern r = ResourcePathPattern.parse(methodPath);
    ResourcePath mr = ResourcePath.parse(messageResource);
    return r.isSatisfiedBy(mr);
  }



  private boolean parameterTypeMatches(Message outboundMessage, Method m) {
    CommandHandlerMethod a = m.getDeclaredAnnotation(CommandHandlerMethod.class);
    if (a == null)
      return false;
    String expectedType = StringUtils.isBlank(a.commandType()) ? findCommandParameterType(m).getName() : a.commandType();
    return expectedType.equals(outboundMessage.getHeader(CommandMessageHeaders.COMMAND_TYPE).get());
  }

  private Class findCommandParameterType(Method m) {
    int i = IntStream
            .range(0, m.getParameterTypes().length)
            .filter(idx -> CommandMessage.class.isAssignableFrom(m.getParameterTypes()[idx]))
            .findFirst()
            .getAsInt();
    return (Class) ((ParameterizedType) m.getGenericParameterTypes()[i]).getActualTypeArguments()[0];
  }

  private Optional<Method> findExceptionHandler(Throwable cause) {
    for (Method m : target.getClass().getMethods()) {
      CommandExceptionHandler a = m.getDeclaredAnnotation(CommandExceptionHandler.class);
      if (a != null) {
        if (m.getParameterTypes()[0].isInstance(cause))
          return Optional.of(m);
      }
    }
    return Optional.empty();
  }
}
