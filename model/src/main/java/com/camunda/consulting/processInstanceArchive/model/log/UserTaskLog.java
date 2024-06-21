package com.camunda.consulting.processInstanceArchive.model.log;

import java.time.LocalDateTime;
import java.util.List;

public record UserTaskLog(LocalDateTime timestamp, String user, String assignee, List<String> candidateGroups,
                          List<String> candidateUsers, LocalDateTime dueDate, LocalDateTime followUpDate, String bpmnError, String bpmnEscalation, Boolean complete) {}
