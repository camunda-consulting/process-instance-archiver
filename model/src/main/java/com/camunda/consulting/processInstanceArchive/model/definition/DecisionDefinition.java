package com.camunda.consulting.processInstanceArchive.model.definition;

import com.camunda.consulting.processInstanceArchive.model.instance.DecisionInstance;

import java.util.List;

public record DecisionDefinition(String key, String name, String dmnXmlId, List<DecisionInstance> decisionInstances,String tenantId) {}
