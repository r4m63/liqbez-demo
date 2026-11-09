package ru.liqbez.patient.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.liqbez.patient.model.Patient;
import ru.liqbez.patient.repository.PatientRepository;

@Service
public class PatientService {
    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public Optional<Patient> find(long id) {
        return repository.find(id);
    }
}
