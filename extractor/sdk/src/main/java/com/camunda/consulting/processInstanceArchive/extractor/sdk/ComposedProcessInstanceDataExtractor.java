package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.model.definition.ProcessEngine;

import java.util.List;

public class ComposedProcessInstanceDataExtractor implements ProcessInstanceDataExtractor {
  private final List<ProcessInstanceDataExtractor> extractors;

  public ComposedProcessInstanceDataExtractor(List<ProcessInstanceDataExtractor> extractors) {
    this.extractors = extractors;
  }

  @Override
  public List<ProcessEngine> extract() {
    return extractors
        .stream()
        .flatMap(e -> e
            .extract()
            .stream())
        .toList();
  }
}
