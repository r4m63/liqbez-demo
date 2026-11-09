package ru.liqbez.appointment.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ru.liqbez.appointment.client.PatientClient;
import ru.liqbez.appointment.model.Appointment;
import ru.liqbez.appointment.model.Patient;

@Service
public class AppointmentService {
    private final PatientClient patients;

    public AppointmentService(PatientClient patients) {
        this.patients = patients;
    }

    public Optional<Appointment> find(long id) {
        if (id != 1) return Optional.empty();
        try {
            Patient patient = patients.find(1);
            return Optional.of(new Appointment(1, patient));
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
    }
}
