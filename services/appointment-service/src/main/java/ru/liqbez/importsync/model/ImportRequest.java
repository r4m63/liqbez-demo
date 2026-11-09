package ru.liqbez.importsync.model;

import java.util.List;

public record ImportRequest(String fileName, List<String> petNames) {}
