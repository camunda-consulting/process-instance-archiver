package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.model.definition.DecisionDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.DecisionRequirementsDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessDefinition;
import com.camunda.consulting.processInstanceArchive.model.instance.DecisionInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.VariableValueInstance;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface ProcessEngineAdapter {
  String id();

  Map<String, String> tags();

  List<DecisionInstanceRef> getRelatedDecisionInstances(DecisionInstanceRef decisionInstanceRef);

  Entry<String, DecisionInstance> getDecisionInstance(String decisionInstanceId);

  Entry<String, DecisionDefinition> getDecisionDefinition(
      String key, List<DecisionInstance> decisionInstances
  );

  DecisionRequirementsDefinition getDecisionRequirementsDefinition(
      String key, List<DecisionDefinition> decisionDefinition
  );

  Map<String, List<ProcessInstance>> getProcessInstances(ProcessInstanceFilter processInstanceFilter);

  ProcessDefinition getProcessDefinition(
      String processDefinitionKey, List<ProcessInstance> processInstances
  );

  Map<String, VariableValueInstance> getVariables(
      String scopeKey
  );

  List<ElementInstance> getElementInstances(String processInstanceKey);

  List<DecisionInstanceRef> getReferences(ElementInstance elementInstance);

  List<DecisionInstanceRef> getReferences(ProcessInstance processInstance);

  ProcessInstance getProcessInstance(ProcessInstanceFilter processInstanceFilter);
}
