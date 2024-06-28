package com.camunda.consulting.processInstanceArchive.model.instance;

import java.util.Map;

public record DecisionInstance(String key, Map<String, Object> inputs, Map<String, DecisionInstanceOutputValue> outputs,
                               String tenantId, String decisionDefinitionKey) {}
