package com.camunda.consulting.processInstanceArchive.model.instance;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ElementInstance(String elementId, String key, InstanceState state, LocalDateTime startDate,
                              LocalDateTime endDate, List<ElementInstance> childElementInstances,
                              Map<String, VariableValueInstance> variables,
                              ElementInstanceExtension extension,
                              Boolean multiInstanceBody) {}
