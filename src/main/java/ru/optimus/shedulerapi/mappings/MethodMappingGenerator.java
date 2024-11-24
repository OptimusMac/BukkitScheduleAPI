package ru.optimus.shedulerapi.mappings;

import java.lang.reflect.Method;

public class MethodMappingGenerator {

  public static String generateMethodMapping(Class<?> clazz, String methodName) throws NoSuchMethodException {
    Method method = clazz.getMethod(methodName);

    return generateUniqueMethodKey(method);
  }

  private static String generateUniqueMethodKey(Method method) {
    StringBuilder keyBuilder = new StringBuilder(method.getName());

    for (Class<?> paramType : method.getParameterTypes()) {
      keyBuilder.append("-").append(paramType.getName());
    }

    return Integer.toHexString(keyBuilder.toString().hashCode());
  }

}
