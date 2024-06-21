package com.camunda.consulting.processInstanceArchive.model.instance;

import java.util.Map;

@ElementInstanceExtensionSubType("message")
public record MessageInstance(String key, String name, String id, String correlationKey, Map<String,Object> variables) implements ElementInstanceExtension {}
