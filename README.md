# Process Instance Archiver

## Purpose

In Camunda 8, the retention period for a process instance is relatively short.

To be able to store process instance data for archiving purposes, it can be extracted and moved to a dedicated data storage (data warehouse).

Also, on migration, the data from Camunda 7 should be extracted as well to keep it together.

So, we have 3 requirements:

* extract completed process instances from Camunda 7
* extract completed process instances from Camunda 8
* extract completed process instances from many engines at the same time without conflicts

## Components

### Model

The most important part about exporting data is a proper model. The model component has a proper and 
