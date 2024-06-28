package com.camunda.consulting.processInstanceArchive.extractor.sdk.handler;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessEngine;

import java.util.Set;

public record ElementInstanceContext(String key, String type, ProcessEngineAdapter processEngineAdapter,
                                     ProcessEngine processEngine,
                                     Set<ElementInstanceExtensionHandler> elementInstanceExtensionHandlers) {

}
