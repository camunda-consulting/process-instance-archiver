package com.camunda.consulting.processInstanceArchive.model.instance;

import com.camunda.consulting.processInstanceArchive.model.log.UserTaskLog;

import java.time.LocalDateTime;
import java.util.List;

@ElementInstanceExtensionSubType("userTask")
public record UserTaskInstance(String formKey, String assignee, List<String> candidateGroups, List<String> candidateUsers,
                               LocalDateTime dueDate, LocalDateTime followUpDate, List<UserTaskLog> changes) implements ElementInstanceExtension {}
