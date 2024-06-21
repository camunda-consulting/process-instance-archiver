package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.model.instance.ElementInstanceExtension;
import com.camunda.consulting.processInstanceArchive.model.instance.JobInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.MessageInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.ProcessInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.SignalInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.TimerInstance;
import com.camunda.consulting.processInstanceArchive.model.instance.UserTaskInstance;
import com.camunda.consulting.processInstanceArchive.model.reference.DecisionInstanceRef;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class ElementInstanceExtensionObjectMapperConfigurerTest {
  @Test
  void shouldProvideAllSubTypes() {
    ElementInstanceExtensionObjectMapperConfigurer configurer = new ElementInstanceExtensionObjectMapperConfigurer();
    Set<Class<? extends ElementInstanceExtension>> classes = configurer.getElementInstanceExtensionSubtypes();
    assertThat(classes).containsExactlyInAnyOrder(MessageInstance.class,
        JobInstance.class,
        DecisionInstanceRef.class,
        SignalInstance.class,
        TimerInstance.class,
        UserTaskInstance.class,
        ProcessInstance.class
    );
  }

  @Test
  void shouldConfigureObjectMapper() throws JsonProcessingException {
    ElementInstanceExtensionObjectMapperConfigurer configurer = new ElementInstanceExtensionObjectMapperConfigurer();
    ObjectMapper objectMapper = new ObjectMapper();
    configurer.configureObjectMapper(objectMapper);
    ElementInstanceExtensionWrapper wrapper = new ElementInstanceExtensionWrapper(new SignalInstance("key",
        "signalName",
        Map.of()
    ));
    String asString = objectMapper.writeValueAsString(wrapper);
    ElementInstanceExtensionWrapper newWrapper = objectMapper.readValue(asString, ElementInstanceExtensionWrapper.class);
    assertThat(newWrapper.extension()).isInstanceOf(SignalInstance.class);
  }

  private record ElementInstanceExtensionWrapper(ElementInstanceExtension extension) {}
}
