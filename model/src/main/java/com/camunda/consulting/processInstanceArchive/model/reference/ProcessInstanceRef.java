package com.camunda.consulting.processInstanceArchive.model.reference;

import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtensionSubType;

@ElementInstanceExtensionSubType("calledProcessInstance")
public record ProcessInstanceRef(String key) implements ElementInstanceExtension {}
