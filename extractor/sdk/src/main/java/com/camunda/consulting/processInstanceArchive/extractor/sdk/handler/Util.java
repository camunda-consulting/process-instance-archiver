package com.camunda.consulting.processInstanceArchive.extractor.sdk.handler;

import java.util.List;

public class Util {
  public static <T> T getFirst(List<T> list, boolean throwOnEmptyList) {
    if (list.isEmpty()) {
      if (throwOnEmptyList) {
        throw new IllegalStateException("Expected exactly one element but list is empty");
      }
      return null;
    }
    if (list.size() == 1) {
      return list.getFirst();
    }
    throw new IllegalStateException("Expected only one element in list but got " + list.size());
  }
}
