package com.camunda.consulting.processInstanceArchive.model.definition;

import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;

import java.util.List;

public record ProcessDefinition(String bpmnXml, String key, String bpmnXmlId ,String name, Long version, String versionTag, String tenantId) {}
