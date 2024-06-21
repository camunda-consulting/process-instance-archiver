package com.camunda.consulting.processInstanceArchive.extractor.camunda8;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ElementInstanceContext;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ElementInstanceExtensionHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessEngineAdapter;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessInstanceFilter;
import com.camunda.consulting.processInstanceArchive.model.definition.DecisionDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.DecisionRequirementsDefinition;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessDefinition;
import com.camunda.consulting.processInstanceArchive.model.instance.DecisionInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.DecisionInstanceOutputValue;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.instance.InstanceState;
import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.VariableValueInstance;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.CamundaOperateClient;
import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.DecisionInstanceInput;
import io.camunda.operate.model.DecisionInstanceOutput;
import io.camunda.operate.model.DecisionRequirements;
import io.camunda.operate.model.FlowNodeInstance;
import io.camunda.operate.model.FlowNodeInstanceState;
import io.camunda.operate.model.ProcessInstanceState;
import io.camunda.operate.model.SearchResult;
import io.camunda.operate.model.Variable;
import io.camunda.operate.search.DecisionInstanceFilter;
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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.*;

public class Camunda8Adapter implements ProcessEngineAdapter {
  private final CamundaOperateClient operateClient;
  private final ObjectMapper objectMapper;
  private final Set<ElementInstanceExtensionHandler> elementInstanceExtensionHandlers;
  private final String id;
  private final Map<String, String> tags;

  public Camunda8Adapter(
      CamundaOperateClient operateClient,
      ObjectMapper objectMapper,
      Set<ElementInstanceExtensionHandler> elementInstanceExtensionHandlers,
      String id,
      Map<String, String> tags
  ) {
    this.operateClient = operateClient;
    this.objectMapper = objectMapper;
    this.elementInstanceExtensionHandlers = elementInstanceExtensionHandlers;
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

  @Override
  public List<DecisionInstanceRef> getRelatedDecisionInstances(DecisionInstanceRef decisionInstanceRef) {
    try {
      return searchData(operateClient::searchDecisionInstanceResults,
          DecisionInstanceFilter
              .builder()
              .key(Long.valueOf(decisionInstanceRef.key()))
              .build()
      )
          .stream()
          .map(di -> new DecisionInstanceRef(di.getId(), decisionInstanceRef.key()))
          .toList();
    } catch (OperateException e) {
      throw new RuntimeException("Error while extracting related decision instances for key " + decisionInstanceRef.key(),
          e
      );
    }
  }

  @Override
  public Entry<String, DecisionInstance> getDecisionInstance(String decisionInstanceId) {
    try {
      io.camunda.operate.model.DecisionInstance decisionInstance = operateClient.getDecisionInstance(decisionInstanceId);
      Map<String, Object> inputs = buildInputs(decisionInstance.getEvaluatedInputs());
      Map<String, DecisionInstanceOutputValue> outputs = buildOutputs(decisionInstance.getEvaluatedOutputs());
      return Map.entry(decisionInstance.getDecisionDefinitionId(),
          new DecisionInstance(decisionInstance.getId(), inputs, outputs, decisionInstance.getTenantId())
      );

    } catch (Exception e) {
      throw new RuntimeException("An error happened while fetching the decision instance " + decisionInstanceId, e);
    }
  }

  @Override
  public Map.Entry<String, DecisionDefinition> getDecisionDefinition(
      String key, List<DecisionInstance> decisionInstances
  ) {
    try {
      io.camunda.operate.model.DecisionDefinition decisionDefinition = operateClient.getDecisionDefinition(Long.parseLong(
          key));
      return Map.entry(String.valueOf(decisionDefinition.getDecisionRequirementsKey()),
          new DecisionDefinition(String.valueOf(decisionDefinition.getKey()),
              decisionDefinition.getName(),
              decisionDefinition.getId(),
              decisionInstances,
              decisionDefinition.getTenantId()
          )
      );
    } catch (Exception e) {
      throw new RuntimeException("An error happened while fetching the decision definition " + key, e);
    }
  }

  @Override
  public DecisionRequirementsDefinition getDecisionRequirementsDefinition(
      String key, List<DecisionDefinition> decisionDefinition
  ) {
    try {
      DecisionRequirements decisionRequirements = operateClient.getDecisionRequirements(Long.parseLong(key));
      String decisionRequirementsXml = operateClient.getDecisionRequirementsXml(Long.parseLong(key));
      return new DecisionRequirementsDefinition(String.valueOf(decisionRequirements.getKey()),
          decisionRequirementsXml,
          decisionRequirements.getId(),
          decisionRequirements.getName(),
          null,
          decisionRequirements.getVersion(),
          decisionDefinition,
          decisionRequirements.getTenantId()
      );
    } catch (OperateException e) {
      throw new RuntimeException("An error happened while fetching the decision requirements definition " + key, e);
    }
  }

  @Override
  public Map<String, List<ProcessInstance>> getProcessInstances(ProcessInstanceFilter processInstanceFilter) {
    io.camunda.operate.search.ProcessInstanceFilter filter = io.camunda.operate.search.ProcessInstanceFilter
        .builder()
        .build();
    try {
      return searchData(operateClient::searchProcessInstanceResults, filter, 1000L)
          .stream()
          .collect(Collectors.groupingBy(pi -> String.valueOf(pi.getProcessDefinitionKey()),
              Collectors.mapping(this::fromOperate, Collectors.toList())
          ));
    } catch (OperateException e) {

      throw new RuntimeException("An error happened while fetching the process instances", e);
    }
  }

  @Override
  public ProcessDefinition getProcessDefinition(
      String processDefinitionKey, List<ProcessInstance> processInstances
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
          processDefinition.getTenantId(),
          processInstances
      );
    } catch (OperateException e) {
      throw new RuntimeException("Error while fetching process definition " + processDefinitionKey, e);
    }
  }

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
  public List<ElementInstance> getElementInstances(String processInstanceKey) {
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
              extractElementInstanceExtension(new ElementInstanceContext(String.valueOf(fni.getKey()), fni.getType())),
              isMultiInstanceBody(fni)
          ))
          .toList();
    } catch (OperateException e) {
      throw new RuntimeException("Error while finding element instances for process instance " + processInstanceKey, e);
    }

  }

  @Override
  public List<DecisionInstanceRef> getReferences(ElementInstance elementInstance) {
    List<DecisionInstanceRef> referencedDecisionInstances = new ArrayList<>();
    ofNullable(elementInstance.extension()).ifPresent(p -> referencedDecisionInstances.addAll(
        elementInstanceExtensionHandlers
            .stream()
            .flatMap(h -> h
                .extractDecisionInstances(p)
                .stream())
            .toList()));
    ofNullable(elementInstance.childElementInstances()).ifPresent(e -> referencedDecisionInstances.addAll(
        extractReferencesForElementInstances(e)));
    return referencedDecisionInstances;
  }

  @Override
  public List<DecisionInstanceRef> getReferences(ProcessInstance processInstance) {
    return extractReferencesForElementInstances(processInstance.elementInstances());
  }

  @Override
  public ProcessInstance getProcessInstance(ProcessInstanceFilter processInstanceFilter) {
    try {
      List<io.camunda.operate.model.ProcessInstance> processInstances = searchData(operateClient::searchProcessInstanceResults,
          new ParentFlowNodeInstanceKeyFilter(Long.valueOf(processInstanceFilter.parentElementKey()))
      );
      if (processInstances.size() == 1) {
        return fromOperate(processInstances.getFirst());
      }
      throw new IllegalStateException("Expected to find exactly one process instance for filter " + processInstanceFilter + ", but were " + processInstances.size());
    } catch (OperateException e) {
      throw new RuntimeException("Error while fetching process instances for filter " + processInstanceFilter, e);
    }
  }

  private List<DecisionInstanceRef> extractReferencesForElementInstances(List<ElementInstance> elementInstances) {
    List<DecisionInstanceRef> referencedDecisionInstances = new ArrayList<>();
    for (ElementInstance elementInstance : elementInstances) {
      referencedDecisionInstances.addAll(getReferences(elementInstance));
    }
    return referencedDecisionInstances;
  }

  private ProcessInstance fromOperate(io.camunda.operate.model.ProcessInstance processInstance) {
    return new ProcessInstance(String.valueOf(processInstance.getKey()),
        LocalDateTime.ofInstant(processInstance
            .getStartDate()
            .toInstant(), ZoneId.systemDefault()),
        LocalDateTime.ofInstant(processInstance
            .getEndDate()
            .toInstant(), ZoneId.systemDefault()),
        processInstance.getTenantId(),
        extractElementInstances(String.valueOf(processInstance.getKey())),
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

  private List<ElementInstance> extractElementInstances(String processInstanceKey) {
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
              extractElementInstanceExtension(new ElementInstanceContext(String.valueOf(fni.getKey()), fni.getType())),
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
    // TODO the api should support "parentKey" on flowNodeInstance
    return List.of();
  }

  private ElementInstanceExtension extractElementInstanceExtension(ElementInstanceContext elementInstanceContext) {
    List<? extends ElementInstanceExtension> list = elementInstanceExtensionHandlers
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

  private Map<String, DecisionInstanceOutputValue> buildOutputs(List<DecisionInstanceOutput> evaluatedOutputs) {
    Map<String, DecisionInstanceOutputValue> outputs = new HashMap<>();
    for (DecisionInstanceOutput output : evaluatedOutputs) {
      if (outputs.containsKey(output.getName())) {
        throw new IllegalStateException("Try to add key " + output.getName() + " twice");
      }
      try {
        outputs.put(output.getName(),
            new DecisionInstanceOutputValue(objectMapper.readTree(output.getValue()),
                output.getRuleId(),
                output.getRuleIndex()
            )
        );
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error while parsing value " + output.getValue() + " to json node", e);
      }
    }
    return outputs;
  }

  private Map<String, Object> buildInputs(List<DecisionInstanceInput> evaluatedInputs) {
    Map<String, Object> inputs = new HashMap<>();
    for (DecisionInstanceInput input : evaluatedInputs) {
      if (inputs.containsKey(input.getName())) {
        throw new IllegalStateException("Try to add key " + input.getName() + " twice");
      }
      try {
        inputs.put(input.getName(), objectMapper.readTree(input.getValue()));
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error while parsing value " + input.getValue() + " to json node", e);
      }
    }
    return inputs;
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
