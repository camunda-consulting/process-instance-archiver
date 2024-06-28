package com.camunda.consulting.processInstanceArchive.extractor.sdk;

import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.DecisionInstanceRefHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.ElementInstanceExtensionHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.JobInstanceHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.MessageInstanceHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.ProcessInstanceRefHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.SignalInstanceHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.TimerInstanceHandler;
import com.camunda.consulting.processInstanceArchive.extractor.sdk.handler.UserTaskInstanceHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

import static org.assertj.core.api.Assertions.*;

public class ElementInstanceExtensionHandlerTest {
  @Test
  void shouldLoadAllElementInstanceExtensionHandlers() {
    List<ElementInstanceExtensionHandler> elementInstanceExtensionHandlers = ServiceLoader
        .load(ElementInstanceExtensionHandler.class)
        .stream()
        .map(Provider::get)
        .toList();
    List<Class<?>> classList = new ArrayList<>();
    elementInstanceExtensionHandlers.forEach(handler -> classList.add(handler.getClass()));
    assertThat(classList).containsExactlyInAnyOrder(
        MessageInstanceHandler.class,
        JobInstanceHandler.class,
        DecisionInstanceRefHandler.class,
        SignalInstanceHandler.class,
        TimerInstanceHandler.class,
        UserTaskInstanceHandler.class,
        ProcessInstanceRefHandler.class
    );
  }
}
