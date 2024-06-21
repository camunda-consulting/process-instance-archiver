package com.camunda.consulting.processInstanceArchive.model.definition;

import com.camunda.consulting.processInstanceArchive.model.instance.DecisionInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;

import java.util.List;
import java.util.Map;

public record ProcessEngine(String id, String product, Map<String, String> tags,
                            List<ProcessDefinition> processDefinitions,
                            List<ProcessInstance> processInstances,
                            List<DecisionRequirementsDefinition> decisionRequirementsDefinitions,
                            List<DecisionInstance> decisionInstances) {}
