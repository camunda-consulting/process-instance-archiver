package com.camunda.consulting.processInstanceArchive.extractor.camunda8;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.AbstractTypedElementInstanceExtensionHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ElementInstanceContext;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.instance.JobInstance;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;

import java.util.List;
import java.util.Optional;

public class JobInstanceHandler extends AbstractTypedElementInstanceExtensionHandler<JobInstance> {
  @Override
  protected List<DecisionInstanceRef> extractDecisionInstancesTyped(JobInstance extension) {
    return List.of();
  }

  @Override
  protected Optional<JobInstance> createExtensionTyped(ElementInstanceContext elementInstanceContext) {
    // TODO there is no way to retrieve the job instance for an element as of now
    return Optional.empty();
  }

  @Override
  protected Class<JobInstance> type() {
    return JobInstance.class;
  }

  @Override
  protected List<String> typeNames() {
    return List.of("SERVICE_TASK", "SEND_TASK", "BUSINESS_RULE_TASK", "SCRIPT_TASK");
  }
}
