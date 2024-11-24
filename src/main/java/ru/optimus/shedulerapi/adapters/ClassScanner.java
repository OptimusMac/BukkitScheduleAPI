package ru.optimus.shedulerapi.adapters;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.SerializationUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;


class ClassScanner {


  public static Set<Class<?>> findClassesInPackage(String basePackage) {
    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage(basePackage))
        .setScanners(new SubTypesScanner(false))
    );

    Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

    classes.removeIf(clazz -> !clazz.getName().startsWith(basePackage));
    System.out.println("Found classes in package " + basePackage + ": " + classes.size());
    return classes;
  }


  public static byte[] getClassAsByteArray(Class<?> clazz){
    return SerializationUtils.serialize(clazz);
  }
}

