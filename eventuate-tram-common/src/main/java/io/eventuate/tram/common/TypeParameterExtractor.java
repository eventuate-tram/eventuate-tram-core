package io.eventuate.tram.common;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeParameterExtractor {

  public static Class<?> extractTypeParameter(Method method) {
    Type[] parameterTypes = method.getGenericParameterTypes();
    if (parameterTypes.length == 0) {
      throw new IllegalArgumentException("Method must have at least one parameter");
    }

    Type firstParam = parameterTypes[0];
    if (!(firstParam instanceof ParameterizedType)) {
      throw new IllegalArgumentException("First parameter must be a generic type");
    }

    ParameterizedType parameterizedType = (ParameterizedType) firstParam;
    Type[] typeArguments = parameterizedType.getActualTypeArguments();
    if (typeArguments.length == 0) {
      throw new IllegalArgumentException("First parameter must have a type argument");
    }

    Type typeArg = typeArguments[0];
    if (!(typeArg instanceof Class<?>)) {
      throw new IllegalArgumentException("Type argument must be a class");
    }

    return (Class<?>) typeArg;
  }
}