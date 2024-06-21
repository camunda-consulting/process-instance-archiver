package com.camunda.consulting.processInstanceArchive.extractor.camunda8;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.AbstractTypedElementInstanceExtensionHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ElementInstanceContext;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.instance.MessageInstance;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;

import java.util.List;
import java.util.Optional;

public class MessageInstanceHandler extends AbstractTypedElementInstanceExtensionHandler<MessageInstance> {
  private final ProcessEngineAdapter processEngineAdapter;

  public MessageInstanceHandler(ProcessEngineAdapter processEngineAdapter) {
    this.processEngineAdapter = processEngineAdapter;
  }

  @Override
  protected List<DecisionInstanceRef> extractDecisionInstancesTyped(MessageInstance extension) {
    return List.of();
  }

  @Override
  protected Optional<MessageInstance> createExtensionTyped(ElementInstanceContext elementInstanceContext) {
    // TODO there is no way to retrieve the message instance for an element as of now
    return Optional.empty();
  }

  @Override
  protected Class<MessageInstance> type() {
    return MessageInstance.class;
  }

  @Override
  protected List<String> typeNames() {
    return List.of("RECEIVE_TASK", "INTERMEDIATE_CATCH_EVENT");
  }
}
