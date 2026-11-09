package ru.liqbez.importsync.model;

import java.util.List;

public record ImportState(ImportOperation operation, List<String> petNames, boolean objectPresent) {}
