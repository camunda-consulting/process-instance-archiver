package com.camunda.consulting.processInstanceArchive.model.instance;

import java.time.LocalDateTime;

public record IncidentInstance(String message, LocalDateTime startDate, LocalDateTime endDate, String type) {}
