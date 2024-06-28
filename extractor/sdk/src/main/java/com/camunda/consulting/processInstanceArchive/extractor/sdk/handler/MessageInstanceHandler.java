package com.camunda.consulting.processInstanceArchive.extractor.sdk.handler;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter.MessageInstanceFilter;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;

import java.util.Optional;

import static com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.Util.*;

public class MessageInstanceHandler implements ElementInstanceExtensionHandler {

  @Override
  public Optional<? extends ElementInstanceExtension> createExtension(ElementInstanceContext elementInstanceContext) {
    return Optional.ofNullable(getFirst(elementInstanceContext
        .processEngineAdapter()
        .getMessageInstances(new MessageInstanceFilter(elementInstanceContext.key())), false));
  }
}
