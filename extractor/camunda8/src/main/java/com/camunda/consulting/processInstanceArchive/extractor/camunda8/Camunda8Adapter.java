package com.camunda.consulting.processInstanceArchive.extractor.camunda8;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.ElementInstanceContext;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.ElementInstanceExtensionHandler;
import com.camunda.consulting.processInstanceArchive.model.definition.DecisionDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.DecisionRequirementsDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessEngine;
import com.camunda.consulting.processInstanceArchive.model.instance.DecisionInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.instance.InstanceState;
import com.camunda.consulting.processInstanceArchive.model.instance.JobInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.MessageInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.SignalInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.TimerInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.UserTaskInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.VariableValueInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.DecisionRequirements;
import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.operate.model.FlowNodeInstanceState;
import io.camunda.operate.model.ProcessInstanceState;
import io.camunda.operate.model.SearchResult;
import io.camunda.operate.model.Variable;
import io.camunda.operate.search.DecisionDefinitionFilter;
import io.camunda.operate.search.Filter;
import io.camunda.operate.search.FlowNodeInstanceFilter;
import io.camunda.operate.search.SearchQuery;
import io.camunda.operate.search.VariableFilter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Camunda8Adapter implements ProcessEngineAdapter {
  private final CamundaOperateClient operateClient;
  private final ObjectMapper objectMapper;
  private final String id;
  private final Map<String, String> tags;

  public Camunda8Adapter(
      CamundaOperateClient operateClient, ObjectMapper objectMapper, String id, Map<String, String> tags
  ) {
    this.operateClient = operateClient;
    this.objectMapper = objectMapper;
    this.id = id;
    this.tags = tags;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public Map<String, String> tags() {
    return tags;
  }

  // DRD

  @Override
  public DecisionRequirementsDefinition getDecisionRequirementsDefinition(DecisionRequirementsDefinitionFilter filter) {
    try {
      DecisionRequirements decisionRequirements = operateClient.getDecisionRequirements(Long.valueOf(
          getDecisionDefinition(filter.decisionDefinitionKey()).getKey()));
      String decisionRequirementsXml = operateClient.getDecisionRequirementsXml(decisionRequirements.getKey());
      return fromOperate(decisionRequirements,
          decisionRequirementsXml,
          getDecisionDefinitions(decisionRequirements.getKey())
      );
    } catch (OperateException e) {
      throw new RuntimeException(e);
    }
  }

  private DecisionRequirementsDefinition fromOperate(
      DecisionRequirements decisionRequirements, String dmnXml, List<DecisionDefinition> decisionDefinitions
  ) {

    return new DecisionRequirementsDefinition(String.valueOf(decisionRequirements.getKey()),
        dmnXml,
        decisionRequirements.getId(),
        decisionRequirements.getName(),
        null,
        decisionRequirements.getVersion(),
        decisionDefinitions,
        decisionRequirements.getTenantId()
    );
  }

  // DD

  private List<DecisionDefinition> getDecisionDefinitions(Long decisionRequirementsKey) {
    try {
      return operateClient
          .searchDecisionDefinitions(new SearchQuery.Builder()
              .filter(DecisionDefinitionFilter
                  .builder()
                  .decisionRequirementsKey(decisionRequirementsKey)
                  .build())
              .build())
          .stream()
          .map(this::fromOperate)
          .toList();
    } catch (OperateException e) {
      throw new RuntimeException("Error while fetching decision definitions for decision requirement " + decisionRequirementsKey,
          e
      );
    }
  }

  private DecisionDefinition fromOperate(io.camunda.operate.model.DecisionDefinition decisionDefinition) {
    return new DecisionDefinition(String.valueOf(decisionDefinition.getKey()),
        decisionDefinition.getName(),
        decisionDefinition.getId(),
        decisionDefinition.getTenantId()
    );
  }

  private Map.Entry<String, DecisionDefinition> getDecisionDefinition(
      String key
  ) {
    try {
      io.camunda.operate.model.DecisionDefinition decisionDefinition = operateClient.getDecisionDefinition(Long.parseLong(
          key));
      return Map.entry(String.valueOf(decisionDefinition.getDecisionRequirementsKey()),
          new DecisionDefinition(String.valueOf(decisionDefinition.getKey()),
              decisionDefinition.getName(),
              decisionDefinition.getId(),
              decisionDefinition.getTenantId()
          )
      );
    } catch (Exception e) {
      throw new RuntimeException("An error happened while fetching the decision definition " + key, e);
    }
  }

  // PD

  @Override
  public ProcessDefinition getProcessDefinition(
      String processDefinitionKey
  ) {
    try {
      io.camunda.operate.model.ProcessDefinition processDefinition = operateClient.getProcessDefinition(Long.valueOf(
          processDefinitionKey));
      String processDefinitionXml = operateClient.getProcessDefinitionXml(Long.valueOf(processDefinitionKey));
      return new ProcessDefinition(processDefinitionXml,
          String.valueOf(processDefinition.getKey()),
          processDefinition.getBpmnProcessId(),
          processDefinition.getName(),
          processDefinition.getVersion(),
          null,
          processDefinition.getTenantId()
      );
    } catch (OperateException e) {
      throw new RuntimeException("Error while fetching process definition " + processDefinitionKey, e);
    }
  }

  // VAR

  @Override
  public Map<String, VariableValueInstance> getVariables(
      String scopeKey
  ) {
    try {
      List<Variable> variables = searchData(operateClient::searchVariableResults,
          VariableFilter
              .builder()
              .scopeKey(Long.valueOf(scopeKey))
              .build()
      )
          .stream()
          .map(v -> {
            if (v.getTruncated()) {
              try {
                return operateClient.getVariable(v.getKey());
              } catch (OperateException e) {
                throw new RuntimeException("Error while fetching variable " + v.getKey(), e);
              }
            } else {
              return v;
            }
          })
          .toList();
      Map<String, VariableValueInstance> variableValues = new HashMap<>();
      for (Variable variable : variables) {
        if (variableValues.containsKey(variable.getName())) {
          throw new IllegalStateException("Try to add key " + variable.getName() + " twice");
        }
        try {
          variableValues.put(variable.getName(),
              new VariableValueInstance(objectMapper.readTree(variable.getValue()), List.of())
          );
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Error while parsing value " + variable.getValue() + " to json node", e);
        }
      }
      return variableValues;
    } catch (OperateException e) {
      throw new RuntimeException("Error while extracting variables for scope key " + scopeKey, e);
    }
  }

  @Override
  public List<ProcessInstance> getProcessInstances(
      ProcessInstanceFilter processInstanceFilter,
      ProcessEngine processEngine,
      Set<ElementInstanceExtensionHandler> elementInstanceExtensionHandlers
  ) {
    io.camunda.operate.search.Filter filter = fromArchiver(processInstanceFilter);
    try {
      return searchData(operateClient::searchProcessInstanceResults, filter, 1000L)
          .stream()
          .filter(pi -> isRelevantProcessInstance(processInstanceFilter.parentElementKey() != null ,pi.getParentKey() != null))
          .map(pi -> fromOperate(pi, processEngine, elementInstanceExtensionHandlers))
          .toList();
    } catch (OperateException e) {
      throw new RuntimeException("An error happened while fetching the process instances", e);
    }
  }

  private boolean isRelevantProcessInstance(boolean filterForParentElementKey, boolean parentElementPresent){
    return filterForParentElementKey == parentElementPresent;
  }

  private io.camunda.operate.search.Filter fromArchiver(ProcessInstanceFilter processInstanceFilter) {
    if (processInstanceFilter.parentElementKey() == null) {
      return io.camunda.operate.search.ProcessInstanceFilter
          .builder()
          .build();
    }
    return new ParentFlowNodeInstanceKeyFilter(Long.valueOf(processInstanceFilter.parentElementKey()));
  }

  @Override
  public List<JobInstance> getJobInstances(JobInstanceFilter jobInstanceFilter) {
    // TODO there is no way to retrieve the job instance for an element as of now
    return List.of();
  }

  @Override
  public List<MessageInstance> getMessageInstances(MessageInstanceFilter messageInstanceFilter) {
    // TODO there is no way to retrieve the message instance for an element as of now
    return List.of();
  }

  @Override
  public List<DecisionInstance> getDecisionInstances(DecisionInstanceFilter decisionInstanceFilter) {
    // TODO there is no way to retrieve the decision instance for an element as of now
    return List.of();
  }

  @Override
  public List<SignalInstance> getSignalInstances(SignalInstanceFilter signalInstanceFilter) {
    // TODO there is no way to retrieve the signal instance for an element instance
    return List.of();
  }

  @Override
  public List<TimerInstance> getTimerInstances(TimerInstanceFilter timerInstanceFilter) {
    // TODO there is no way to retrieve a timer instance for an element instance
    return List.of();
  }

  @Override
  public List<UserTaskInstance> getUserTaskInstances(UserTaskInstanceFilter userTaskInstanceFilter) {
    // TODO there is no way to retrieve a user task instance for an element instance
    return List.of();
  }

  private ProcessInstance fromOperate(
      io.camunda.operate.model.ProcessInstance processInstance,
      ProcessEngine processEngine,
      Set<ElementInstanceExtensionHandler> extensionHandlers
  ) {
    return new ProcessInstance(String.valueOf(processInstance.getKey()),
        String.valueOf(processInstance.getProcessDefinitionKey()),
        LocalDateTime.ofInstant(processInstance
            .getStartDate()
            .toInstant(), ZoneId.systemDefault()),
        LocalDateTime.ofInstant(processInstance
            .getEndDate()
            .toInstant(), ZoneId.systemDefault()),
        processInstance.getTenantId(),
        extractElementInstances(String.valueOf(processInstance.getKey()), processEngine, extensionHandlers),
        getVariables(String.valueOf(processInstance.getKey())),
        null,
        mapState(processInstance.getState())
    );
  }

  private InstanceState mapState(ProcessInstanceState state) {
    switch (state) {
    case COMPLETED -> {
      return InstanceState.COMPLETED;
    }
    case CANCELED -> {return InstanceState.CANCELLED;}
    case null, default -> throw new IllegalStateException("State " + state + " not supported");
    }
  }

  private InstanceState mapState(FlowNodeInstanceState state) {
    switch (state) {
    case COMPLETED -> {
      return InstanceState.COMPLETED;
    }
    case TERMINATED -> {return InstanceState.CANCELLED;}
    case null, default -> throw new IllegalStateException("State " + state + " not supported");
    }
  }

  private List<ElementInstance> extractElementInstances(
      String processInstanceKey,
      ProcessEngine processEngine,
      Set<ElementInstanceExtensionHandler> elementInstanceExtensionHandlers
  ) {
    try {
      return searchData(operateClient::searchFlowNodeInstanceResults,
          FlowNodeInstanceFilter
              .builder()
              .processInstanceKey(Long.valueOf(processInstanceKey))
              .build()
      )
          .stream()
          .map(fni -> new ElementInstance(fni.getFlowNodeId(),
              String.valueOf(fni.getKey()),
              mapState(fni.getState()),
              LocalDateTime.ofInstant(fni
                  .getStartDate()
                  .toInstant(), ZoneId.systemDefault()),
              LocalDateTime.ofInstant(fni
                  .getEndDate()
                  .toInstant(), ZoneId.systemDefault()),
              extractChildElementInstances(fni),
              getVariables(String.valueOf(fni.getKey())),
              extractElementInstanceExtension(new ElementInstanceContext(String.valueOf(fni.getKey()),
                  fni.getType(),
                  this,
                  processEngine,
                  elementInstanceExtensionHandlers
              )),
              isMultiInstanceBody(fni)
          ))
          .toList();
    } catch (OperateException e) {
      throw new RuntimeException("Error while finding element instances for process instance " + processInstanceKey, e);
    }
  }

  private boolean isMultiInstanceBody(FlowNodeInstance fni) {
    return "MULTI_INSTANCE_BODY".equals(fni.getType());
  }

  private List<ElementInstance> extractChildElementInstances(FlowNodeInstance flowNodeInstance) {
    // TODO there is no way to retrieve the child element instances of an element instance
    return List.of();
  }

  private ElementInstanceExtension extractElementInstanceExtension(ElementInstanceContext elementInstanceContext) {
    List<? extends ElementInstanceExtension> list = elementInstanceContext
        .elementInstanceExtensionHandlers()
        .stream()
        .map(h -> h.createExtension(elementInstanceContext))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
    if (list.size() == 1) {
      return list.getFirst();
    }
    if (list.isEmpty()) {
      return null;
    }
    throw new IllegalStateException("Expected to get max 1 element instance extension, but got " + list.size() + " for " + elementInstanceContext + ": " + list);
  }

  private <T> List<T> searchData(
      OperateFunction<SearchQuery, SearchResult<T>> searchResultFunction, Filter filter, Long limit
  ) throws OperateException {
    List<T> results = new ArrayList<>();
    SearchQuery searchQuery = new SearchQuery.Builder()
        .filter(filter)
        .size(100)
        .build();
    boolean completed = false;
    while (!completed) {
      SearchResult<T> searchResult = searchResultFunction.apply(searchQuery);
      results.addAll(searchResult.getItems());
      completed = limit == null || limit < 1 ?
          searchResult
              .getItems()
              .isEmpty() :
          results.size() + searchResult
              .getItems()
              .size() <= limit;
      if (!completed) {
        searchQuery.setSearchAfter(searchResult.getSortValues());
      }
    }
    return results;
  }

  private <T> List<T> searchData(
      OperateFunction<SearchQuery, SearchResult<T>> searchResultFunction, Filter filter
  ) throws OperateException {
    return searchData(searchResultFunction, filter, 0L);
  }

  private interface OperateFunction<I, O> {
    O apply(I input) throws OperateException;
  }

  public record ParentFlowNodeInstanceKeyFilter(Long parentFlowNodeInstanceKey) implements Filter {}

}
