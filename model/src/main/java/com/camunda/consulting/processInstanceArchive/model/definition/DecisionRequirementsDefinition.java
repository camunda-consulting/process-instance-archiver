package com.camunda.consulting.processInstanceArchive.model.definition;

import java.util.List;

public record DecisionRequirementsDefinition(String key, String dmnXml, String dmnXmlId, String name, String versionTag,
                                             Long version, List<DecisionDefinition> decisionDefinitions,
                                             String tenantId) {}
