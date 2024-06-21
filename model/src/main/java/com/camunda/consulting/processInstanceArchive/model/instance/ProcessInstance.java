package com.camunda.consulting.processInstanceArchive.model.instance;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ElementInstanceExtensionSubType("calledProcess")
public record ProcessInstance(String key,String processDefinitionKey, LocalDateTime startDate, LocalDateTime endDate, String tenantId, List<ElementInstance> elementInstances, Map<String, VariableValueInstance> variables, String businessKey, InstanceState state) implements ElementInstanceExtension{}
