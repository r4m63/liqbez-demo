package ru.liqbez.patient.repository;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import ru.liqbez.patient.model.Patient;

@Repository
public class PatientRepository {
    private final Map<Long, Patient> patients = Map.of(1L, new Patient(1, "Барсик"));

    public Optional<Patient> find(long id) {
        return Optional.ofNullable(patients.get(id));
    }
}
