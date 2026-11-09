package ru.liqbez.appointment.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ru.liqbez.appointment.client.PatientClient;
import ru.liqbez.appointment.model.Appointment;
import ru.liqbez.appointment.model.Patient;
import ru.liqbez.appointment.repository.AppointmentRepository;

@Service
public class AppointmentService {
    private final PatientClient patients;
    private final AppointmentRepository repository;

    public AppointmentService(PatientClient patients, AppointmentRepository repository) {
        this.patients = patients;
        this.repository = repository;
    }

    public Optional<Appointment> find(long id) {
        if (!repository.exists(id)) return Optional.empty();
        try {
            Patient patient = patients.find(1);
            return Optional.of(new Appointment(id, patient));
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
    }
}
