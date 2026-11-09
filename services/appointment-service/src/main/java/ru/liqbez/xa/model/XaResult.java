package ru.liqbez.xa.model;

import java.util.UUID;

public record XaResult(UUID id, String status, boolean patientDatabase, boolean billingDatabase) {}
