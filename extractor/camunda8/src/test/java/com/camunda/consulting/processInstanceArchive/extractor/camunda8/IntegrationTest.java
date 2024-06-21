package com.camunda.consulting.processInstanceArchive.extractor.camunda8;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.ElementInstanceExtensionHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ElementInstanceExtensionObjectMapperConfigurer;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessInstanceDataExtractor;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessInstanceDataExtractorImpl;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessInstanceFilter;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.ProcessInstanceHandler;
import com.camunda.consulting.processInstanceArchive.model.definition.ProcessEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.common.auth.Product;
import io.camunda.common.auth.SimpleAuthentication;
import io.camunda.common.auth.SimpleConfig;
import io.camunda.common.auth.SimpleCredential;
import io.camunda.operate.CamundaOperateClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IntegrationTest {
  private static @NotNull ProcessInstanceDataExtractor getProcessInstanceDataExtractor(CamundaOperateClient operateClient) {
    ObjectMapper objectMapper = new ObjectMapper();
    ElementInstanceExtensionObjectMapperConfigurer configurer = new ElementInstanceExtensionObjectMapperConfigurer();
    configurer.configureObjectMapper(objectMapper);
    Set<ElementInstanceExtensionHandler> extensionHandlers = new HashSet<>();
    Camunda8Adapter camunda8Adapter = new Camunda8Adapter(operateClient,
        objectMapper,
        extensionHandlers,
        "test",
        Map.of()
    );
    extensionHandlers.add(new ProcessInstanceHandler(camunda8Adapter));
    return new ProcessInstanceDataExtractorImpl(camunda8Adapter, new ProcessInstanceFilter(null));
  }

  @Test
  void shouldExtractProcessInstances() {
    SimpleConfig simpleConfig = new SimpleConfig();
    simpleConfig.addProduct(Product.OPERATE, new SimpleCredential("http://localhost:8081", "demo", "demo"));
    CamundaOperateClient operateClient = CamundaOperateClient
        .builder()
        .operateUrl("http://localhost:8081")
        .authentication(SimpleAuthentication
            .builder()
            .withSimpleConfig(simpleConfig)
            .build())
        .setup()
        .build();
    ProcessInstanceDataExtractor extractor = getProcessInstanceDataExtractor(operateClient);
    List<ProcessEngine> extract = extractor.extract();
  }
}
