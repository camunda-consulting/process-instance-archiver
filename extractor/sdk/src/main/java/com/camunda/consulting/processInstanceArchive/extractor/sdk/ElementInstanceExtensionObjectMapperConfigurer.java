package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtensionSubType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementInstanceExtensionObjectMapperConfigurer {
  private static final Logger LOG = LoggerFactory.getLogger(ElementInstanceExtensionObjectMapperConfigurer.class);
  static String MODEL_PACKAGE = "com.camunda.consulting.processInstanceArchive.model";
  private final Reflections[] reflections;

  public ElementInstanceExtensionObjectMapperConfigurer(Reflections... reflections) {
    this.reflections = reflections;
  }

  public ElementInstanceExtensionObjectMapperConfigurer() {
    this(new Reflections(MODEL_PACKAGE));
  }

  public ElementInstanceExtensionObjectMapperConfigurer(String... basePackages) {
    this(Arrays
        .stream(basePackages)
        .map(Reflections::new)
        .toArray(Reflections[]::new));
  }

  public void configureObjectMapper(ObjectMapper objectMapper) {
    Set<Class<? extends ElementInstanceExtension>> subtypes = getElementInstanceExtensionSubtypes();

    for (Class<? extends ElementInstanceExtension> subType : subtypes) {
      ElementInstanceExtensionSubType annotation = subType.getAnnotation(ElementInstanceExtensionSubType.class);
      if (annotation != null) {
        String typeName = annotation.value();
        objectMapper.registerSubtypes(new NamedType(subType, typeName));
      }
    }
  }

  public Set<Class<? extends ElementInstanceExtension>> getElementInstanceExtensionSubtypes() {
    return Arrays
        .stream(reflections)
        .flatMap(r -> r
            .getSubTypesOf(ElementInstanceExtension.class)
            .stream())
        .peek(type -> LOG.debug("Found element instance extension subtype {}", type))
        .collect(Collectors.toSet());
  }

}
