package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter.ProcessInstanceFilter;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.ElementInstanceExtensionHandler;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessEngine;
import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProcessInstanceDataExtractorImpl implements ProcessInstanceDataExtractor {
  private final ProcessEngineAdapter processEngineAdapter;
  private final ProcessInstanceFilter processInstanceFilter;
  private final Set<ElementInstanceExtensionHandler> elementInstanceExtensionHandlers;

  public ProcessInstanceDataExtractorImpl(
      ProcessEngineAdapter processEngineAdapter,
      ProcessInstanceFilter processInstanceFilter,
      Set<ElementInstanceExtensionHandler> elementInstanceExtensionHandlers
  ) {
    this.processEngineAdapter = processEngineAdapter;
    this.processInstanceFilter = processInstanceFilter;
    this.elementInstanceExtensionHandlers = elementInstanceExtensionHandlers;
  }

  public ProcessInstanceDataExtractorImpl(
      ProcessEngineAdapter processEngineAdapter, ProcessInstanceFilter processInstanceFilter
  ) {
    this(processEngineAdapter, processInstanceFilter, ElementInstanceExtensionHandler.load());
  }

  @Override
  public List<ProcessEngine> extract() {
    return List.of(extractProcessEngine());
  }

  private ProcessEngine extractProcessEngine() {
    ProcessEngine processEngine = new ProcessEngine(processEngineAdapter.id(),
        "camunda8",
        processEngineAdapter.tags(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>(),
        new ArrayList<>()
    );
    extractProcessInstances(processEngine);
    return processEngine;
  }

  private void extractProcessInstances(ProcessEngine processEngine) {
    List<ProcessInstance> processInstances = processEngineAdapter.getProcessInstances(processInstanceFilter,
        processEngine,
        elementInstanceExtensionHandlers
    );
    processEngine
        .processInstances()
        .addAll(processInstances);
    processEngine
        .processDefinitions()
        .addAll(processInstances
            .stream()
            .map(ProcessInstance::processDefinitionKey)
            .distinct()
            .filter(processDefinitionKey -> isNotCollected(processDefinitionKey, processEngine.processDefinitions()))
            .map(processEngineAdapter::getProcessDefinition)
            .toList());
  }

  private boolean isNotCollected(String processDefinitionKey, List<ProcessDefinition> processDefinitions) {
    return processDefinitions
        .stream()
        .noneMatch(pd -> pd
            .key()
            .equals(processDefinitionKey));
  }
}
