package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.model.definition.DecisionDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.DecisionRequirementsDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessEngine;
import com.camunda.consulting.processInstanceArchive.model.instance.DecisionInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessInstanceDataExtractorImpl implements ProcessInstanceDataExtractor {
  private final ProcessEngineAdapter processEngineAdapter;
  private final ProcessInstanceFilter processInstanceFilter;

  public ProcessInstanceDataExtractorImpl(
      ProcessEngineAdapter processEngineAdapter, ProcessInstanceFilter processInstanceFilter
  ) {
    this.processEngineAdapter = processEngineAdapter;
    this.processInstanceFilter = processInstanceFilter;
  }

  @Override
  public List<ProcessEngine> extract() {
    return List.of(extractProcessEngine());
  }

  private ProcessEngine extractProcessEngine() {
    List<ProcessDefinition> processDefinitions = extractProcessDefinitions();
    List<DecisionInstanceRef> referencedDecisionInstances = extractReferencesForProcessDefinitions(processDefinitions);
    return new ProcessEngine(
        processEngineAdapter.id(),
        "camunda8",
        processEngineAdapter.tags(),
        processDefinitions,
        extractDecisionRequirementsDefinitions(referencedDecisionInstances)
    );
  }

  private List<DecisionInstanceRef> extractReferencesForProcessDefinitions(List<ProcessDefinition> processDefinitions) {
    List<DecisionInstanceRef> referencedDecisionInstances = new ArrayList<>();
    for (ProcessDefinition processDefinition : processDefinitions) {
      referencedDecisionInstances.addAll(extractReferences(processDefinition));
    }
    return referencedDecisionInstances
        .stream()
        .distinct()
        .toList();
  }

  private List<DecisionInstanceRef> extractReferences(ProcessDefinition processDefinition) {
    return extractReferencesForProcessInstances(processDefinition.processInstances());
  }

  private List<DecisionInstanceRef> extractReferencesForProcessInstances(List<ProcessInstance> processInstances) {
    List<DecisionInstanceRef> referencedDecisionInstances = new ArrayList<>();
    for (ProcessInstance processInstance : processInstances) {
      referencedDecisionInstances.addAll(processEngineAdapter.getReferences(processInstance));
    }
    return referencedDecisionInstances;
  }



  private List<DecisionRequirementsDefinition> extractDecisionRequirementsDefinitions(
      List<DecisionInstanceRef> referencedDecisionInstances
  ) {
    // find all decision instances
    Map<String, List<DecisionInstance>> decisionInstances = extractDecisionInstances(referencedDecisionInstances);
    // extract their decision definitions
    Map<String, List<DecisionDefinition>> decisionDefinitions = decisionInstances
        .entrySet()
        .stream()
        .map(e -> processEngineAdapter.getDecisionDefinition(e.getKey(), e.getValue()))
        .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList())));
    // extract their decision requirement definitions
    return decisionDefinitions
        .entrySet()
        .stream()
        .map(e -> processEngineAdapter.getDecisionRequirementsDefinition(e.getKey(), e.getValue()))
        .toList();
  }

  private Map<String, List<DecisionInstance>> extractDecisionInstances(
      List<DecisionInstanceRef> referencedDecisionInstances
  ) {
    return referencedDecisionInstances
        .stream()
        .flatMap(this::extractRelatedDecisionInstances)
        .map(DecisionInstanceRef::key)
        .map(processEngineAdapter::getDecisionInstance)
        .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList())));
  }

  private Stream<DecisionInstanceRef> extractRelatedDecisionInstances(
      DecisionInstanceRef ref
  ) {
    return processEngineAdapter
        .getRelatedDecisionInstances(ref)
        .stream();
  }

  private List<ProcessDefinition> extractProcessDefinitions() {
    Map<String, List<ProcessInstance>> processInstancesByDefinitionKey = processEngineAdapter.getProcessInstances(
        processInstanceFilter);
    return processInstancesByDefinitionKey
        .entrySet()
        .stream()
        .map(e -> processEngineAdapter.getProcessDefinition(e.getKey(), e.getValue()))
        .toList();
  }


}
