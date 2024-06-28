package com.camunda.consulting.processInstanceArchive.extractor.sdk.handler;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter.ProcessInstanceFilter;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessDefinition;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;
import com.camunda.consulting.processInstanceArchive.model.reference.ProcessInstanceRef;

import java.util.List;
import java.util.Optional;

import static com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.Util.*;

public class ProcessInstanceRefHandler implements ElementInstanceExtensionHandler {

  @Override
  public Optional<? extends ElementInstanceExtension> createExtension(ElementInstanceContext elementInstanceContext) {
    if ("CALL_ACTIVITY".equals(elementInstanceContext.type())) {
      ProcessInstance processInstance = getFirst(elementInstanceContext
          .processEngineAdapter()
          .getProcessInstances(
              new ProcessInstanceFilter(elementInstanceContext.key()),
              elementInstanceContext.processEngine(),
              elementInstanceContext.elementInstanceExtensionHandlers()
          ), true);
      elementInstanceContext
          .processEngine()
          .processInstances()
          .add(processInstance);
      if (processDefinitionUnknown(elementInstanceContext
          .processEngine()
          .processDefinitions(), processInstance.processDefinitionKey())) {
        ProcessDefinition processDefinition = elementInstanceContext
            .processEngineAdapter()
            .getProcessDefinition(processInstance.processDefinitionKey());
        elementInstanceContext
            .processEngine()
            .processDefinitions()
            .add(processDefinition);
      }
      return Optional.of(new ProcessInstanceRef(processInstance.key()));
    }
    return Optional.empty();
  }

  private boolean processDefinitionUnknown(List<ProcessDefinition> processDefinitions, String processDefinitionKey) {
    return processDefinitions.isEmpty() || processDefinitions
        .stream()
        .noneMatch(pd -> pd
            .key()
            .equals(processDefinitionKey));
  }
}
