package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.ElementInstanceExtensionHandler;
import com.camunda.consulting.processInstanceArchive.model.definition.DecisionRequirementsDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessEngine;
import com.camunda.consulting.processInstanceArchive.model.instance.DecisionInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.JobInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.MessageInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.SignalInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.TimerInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.UserTaskInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.VariableValueInstance;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProcessEngineAdapter {
  String id();

  Map<String, String> tags();

  DecisionRequirementsDefinition getDecisionRequirementsDefinition(DecisionRequirementsDefinitionFilter filter);

  ProcessDefinition getProcessDefinition(String processDefinitionKey);

  Map<String, VariableValueInstance> getVariables(
      String scopeKey
  );

  List<ProcessInstance> getProcessInstances(
      ProcessInstanceFilter processInstanceFilter,
      ProcessEngine processEngine,
      Set<ElementInstanceExtensionHandler> elementInstanceExtensionHandlers
  );

  List<JobInstance> getJobInstances(JobInstanceFilter jobInstanceFilter);

  List<MessageInstance> getMessageInstances(MessageInstanceFilter messageInstanceFilter);

  List<DecisionInstance> getDecisionInstances(DecisionInstanceFilter decisionInstanceFilter);

  List<SignalInstance> getSignalInstances(SignalInstanceFilter signalInstanceFilter);

  List<TimerInstance> getTimerInstances(TimerInstanceFilter timerInstanceFilter);

  List<UserTaskInstance> getUserTaskInstances(UserTaskInstanceFilter userTaskInstanceFilter);

  record ProcessInstanceFilter(String parentElementKey) {}

  record JobInstanceFilter(String elementInstanceKey) {}

  record DecisionRequirementsDefinitionFilter(String decisionDefinitionKey) {}

  record MessageInstanceFilter(String elementInstanceKey) {}

  record DecisionInstanceFilter(String elementInstanceKey) {}

  record SignalInstanceFilter(String elementInstanceKey) {}

  record TimerInstanceFilter(String elementInstanceKey) {}

  record UserTaskInstanceFilter(String elementInstanceKey) {}
}
