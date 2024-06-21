package com.camunda.consulting.processInstanceArchive.model.reference;

import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtensionSubType;

@ElementInstanceExtensionSubType("decisionInstance")
public record DecisionInstanceRef(String id, String key) implements ElementInstanceExtension {}
