package ru.liqbez.appointment.model;

import java.util.UUID;

public record XaResult(UUID id, String status, boolean patientDatabase, boolean billingDatabase) {}
