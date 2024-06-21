package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;

import java.util.List;
import java.util.Optional;

public interface ElementInstanceExtensionHandler {
  List<DecisionInstanceRef> extractDecisionInstances(ElementInstanceExtension extension);

  Optional<? extends ElementInstanceExtension> createExtension(ElementInstanceContext elementInstanceContext);
}
