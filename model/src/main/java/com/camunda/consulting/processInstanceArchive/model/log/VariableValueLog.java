package com.camunda.consulting.processInstanceArchive.model.log;

import java.time.LocalDateTime;

public record VariableValueLog(String changedAtElementInstanceKey, LocalDateTime timestamp, Object newValue) {}
