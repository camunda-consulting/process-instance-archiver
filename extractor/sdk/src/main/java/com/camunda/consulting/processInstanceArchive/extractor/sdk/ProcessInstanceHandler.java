package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;

import java.util.List;
import java.util.Optional;

public class ProcessInstanceHandler extends AbstractTypedElementInstanceExtensionHandler<ProcessInstance> {
  private final ProcessEngineAdapter processEngineAdapter;

  public ProcessInstanceHandler(ProcessEngineAdapter processEngineAdapter) {
    this.processEngineAdapter = processEngineAdapter;
  }

  @Override
  protected List<DecisionInstanceRef> extractDecisionInstancesTyped(ProcessInstance extension) {
    return processEngineAdapter.getReferences(extension);
  }

  @Override
  protected Optional<ProcessInstance> createExtensionTyped(ElementInstanceContext elementInstanceContext) {
    return Optional.of(processEngineAdapter.getProcessInstance(new ProcessInstanceFilter(elementInstanceContext.key())));
  }

  @Override
  protected Class<ProcessInstance> type() {
    return ProcessInstance.class;
  }

  @Override
  protected List<String> typeNames() {
    return List.of("CALL_ACTIVITY");
  }
}
