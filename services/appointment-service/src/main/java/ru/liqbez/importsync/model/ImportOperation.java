package ru.liqbez.importsync.model;

import java.util.UUID;

public record ImportOperation(UUID id, String status, int importedCount, String objectKey, String fileName) {}
