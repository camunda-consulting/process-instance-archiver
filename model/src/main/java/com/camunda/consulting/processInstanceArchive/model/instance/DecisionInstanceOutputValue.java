package com.camunda.consulting.processInstanceArchive.model.instance;

public record DecisionInstanceOutputValue(Object value,String ruleId, Long ruleIndex) {}
