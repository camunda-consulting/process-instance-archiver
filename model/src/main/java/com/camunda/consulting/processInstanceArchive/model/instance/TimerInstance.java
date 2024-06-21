package com.camunda.consulting.processInstanceArchive.model.instance;

import java.time.LocalDateTime;

@ElementInstanceExtensionSubType("timer")
public record TimerInstance(LocalDateTime dueDate) implements ElementInstanceExtension {}
