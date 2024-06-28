package com.camunda.consulting.processInstanceArchive.extractor.sdk.handler;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter.JobInstanceFilter;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;

import java.util.Optional;

import static com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.Util.*;

public class JobInstanceHandler implements ElementInstanceExtensionHandler {

  @Override
  public Optional<? extends ElementInstanceExtension> createExtension(ElementInstanceContext elementInstanceContext) {
    return Optional.ofNullable(getFirst(elementInstanceContext
        .processEngineAdapter()
        .getJobInstances(new JobInstanceFilter(elementInstanceContext.key())), false));

  }
}
