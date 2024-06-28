package com.camunda.consulting.processInstanceArchive.extractor.sdk.handler;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter.DecisionInstanceFilter;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter.DecisionRequirementsDefinitionFilter;
import com.camunda.consulting.processInstanceArchive.model.definition.DecisionRequirementsDefinition;
import com.camunda.consulting.processInstanceArchive.model.instance.DecisionInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;

import java.util.List;
import java.util.Optional;

import static com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.Util.*;

public class DecisionInstanceRefHandler implements ElementInstanceExtensionHandler {
  @Override
  public Optional<? extends ElementInstanceExtension> createExtension(ElementInstanceContext elementInstanceContext) {
    if (isDecisionInstance(elementInstanceContext.type())) {
      DecisionInstance decisionInstance = getFirst(elementInstanceContext
          .processEngineAdapter()
          .getDecisionInstances(new DecisionInstanceFilter(elementInstanceContext.key())), false);
      elementInstanceContext
          .processEngine()
          .decisionInstances()
          .add(decisionInstance);
      if (decisionDefinitionUnknown(elementInstanceContext
          .processEngine()
          .decisionRequirementsDefinitions(), decisionInstance.decisionDefinitionKey())) {
        DecisionRequirementsDefinition drd = elementInstanceContext
            .processEngineAdapter()
            .getDecisionRequirementsDefinition(new DecisionRequirementsDefinitionFilter(decisionInstance.decisionDefinitionKey()));
        elementInstanceContext
            .processEngine()
            .decisionRequirementsDefinitions()
            .add(drd);
      }
      return Optional.of(new DecisionInstanceRef(decisionInstance.key()));
    }
    return Optional.empty();
  }

  private boolean decisionDefinitionUnknown(
      List<DecisionRequirementsDefinition> decisionRequirementsDefinitions, String decisionDefinitionKey
  ) {
    return decisionRequirementsDefinitions.isEmpty() || decisionRequirementsDefinitions
        .stream()
        .flatMap(drd -> drd
            .decisionDefinitions()
            .stream())
        .anyMatch(dd -> dd
            .key()
            .equals(decisionDefinitionKey));
  }

  private boolean isDecisionInstance(String type) {
    return "BUSINESS_RULE_TASK".equals(type);
  }
}
