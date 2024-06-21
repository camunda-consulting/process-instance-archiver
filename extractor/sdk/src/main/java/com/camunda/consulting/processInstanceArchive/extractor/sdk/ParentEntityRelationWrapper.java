package com.camunda.consulting.processInstanceArchive.extractor.sdk;

public record ParentEntityRelationWrapper<T>(String parentId, T entity) {}
