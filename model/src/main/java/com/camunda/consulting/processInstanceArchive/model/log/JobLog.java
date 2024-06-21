package com.camunda.consulting.processInstanceArchive.model.log;

import java.time.Duration;
import java.time.LocalDateTime;

public record JobLog(LocalDateTime timestamp, Integer retries, String jobWorker, Duration backoff, LocalDateTime timeout, String bpmnError, Boolean complete, String bpmnEscalation) {}
