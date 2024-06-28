package com.camunda.consulting.processInstanceArchive.extractor.sdk.handler;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter.TimerInstanceFilter;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;

import java.util.Optional;

import static com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.Util.*;

public class TimerInstanceHandler implements ElementInstanceExtensionHandler {

  @Override
  public Optional<? extends ElementInstanceExtension> createExtension(ElementInstanceContext elementInstanceContext) {
    return Optional.ofNullable(getFirst(elementInstanceContext
        .processEngineAdapter()
        .getTimerInstances(new TimerInstanceFilter(elementInstanceContext.key())), false));
  }
}
