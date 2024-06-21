package com.camunda.consulting.processInstanceArchive.model.instance;

import java.util.Map;

@ElementInstanceExtensionSubType("signal")
public record SignalInstance(String key, String name, Map<String,Object> variables) implements ElementInstanceExtension {}
