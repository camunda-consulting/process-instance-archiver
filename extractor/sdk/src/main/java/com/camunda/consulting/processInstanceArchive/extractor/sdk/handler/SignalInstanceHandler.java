package com.camunda.consulting.processInstanceArchive.extractor.sdk.handler;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter.SignalInstanceFilter;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;

import java.util.Optional;

import static com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.Util.*;

public class SignalInstanceHandler implements ElementInstanceExtensionHandler {
  @Override
  public Optional<? extends ElementInstanceExtension> createExtension(ElementInstanceContext elementInstanceContext) {
    return Optional.ofNullable(getFirst(elementInstanceContext
        .processEngineAdapter()
        .getSignalInstances(new SignalInstanceFilter(elementInstanceContext.key())), false));
  }
}
