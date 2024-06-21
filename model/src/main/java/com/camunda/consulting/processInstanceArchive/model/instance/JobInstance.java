package com.camunda.consulting.processInstanceArchive.model.instance;

import com.camunda.consulting.processInstanceArchive.model.log.JobLog;

import java.util.List;

@ElementInstanceExtensionSubType("job")
public record JobInstance(String jobType, List<JobLog> jobLog, List<IncidentInstance> incidents)
    implements ElementInstanceExtension {}
