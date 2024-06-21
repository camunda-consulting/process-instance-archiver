package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;

import java.util.List;
import java.util.Optional;

public abstract class AbstractTypedElementInstanceExtensionHandler<T extends ElementInstanceExtension>
    implements ElementInstanceExtensionHandler {
  @Override
  public final List<DecisionInstanceRef> extractDecisionInstances(ElementInstanceExtension extension) {
    if (type().isAssignableFrom(extension.getClass())) {
      return extractDecisionInstancesTyped((T) extension);
    }
    return List.of();
  }

  @Override
  public final Optional<? extends ElementInstanceExtension> createExtension(ElementInstanceContext elementInstanceContext) {
    if (typeNames().contains(elementInstanceContext.type())) {
      return createExtensionTyped(elementInstanceContext);
    }
    return Optional.empty();
  }

  protected abstract List<DecisionInstanceRef> extractDecisionInstancesTyped(T extension);

  protected abstract Optional<T> createExtensionTyped(ElementInstanceContext elementInstanceContext);

  protected abstract Class<T> type();

  protected abstract List<String> typeNames();
}
