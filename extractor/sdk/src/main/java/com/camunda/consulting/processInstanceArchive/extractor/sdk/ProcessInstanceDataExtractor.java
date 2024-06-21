package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.model.definition.ProcessEngine;

import java.util.List;

public interface ProcessInstanceDataExtractor {
  public List<ProcessEngine> extract();
}
