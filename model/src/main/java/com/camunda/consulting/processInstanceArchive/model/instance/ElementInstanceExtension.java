package com.camunda.consulting.processInstanceArchive.model.instance;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, property = "type")
public interface ElementInstanceExtension {}
