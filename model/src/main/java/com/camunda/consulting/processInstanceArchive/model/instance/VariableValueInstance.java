package com.camunda.consulting.processInstanceArchive.model.instance;

import com.camunda.consulting.processInstanceArchive.model.log.VariableValueLog;

import java.util.List;

public record VariableValueInstance(Object value, List<VariableValueLog> changes) {}
