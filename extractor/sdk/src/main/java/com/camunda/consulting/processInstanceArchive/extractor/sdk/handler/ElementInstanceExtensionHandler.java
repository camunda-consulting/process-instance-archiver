package com.camunda.consulting.processInstanceArchive.extractor.sdk.handler;

import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.stream.Collectors;

public interface ElementInstanceExtensionHandler {

  static Set<ElementInstanceExtensionHandler> load() {
    return ServiceLoader
        .load(ElementInstanceExtensionHandler.class)
        .stream()
        .map(Provider::get)
        .collect(Collectors.toSet());
  }

  Optional<? extends ElementInstanceExtension> createExtension(ElementInstanceContext elementInstanceContext);
}
