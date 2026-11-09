package ru.liqbez.appointment;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final RestClient patients;

    public AppointmentController(@Qualifier("patientRestClientBuilder") RestClient.Builder builder) {
        this.patients = builder.baseUrl("http://patient-service").build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> find(@PathVariable long id) {
        if (id != 1) return ResponseEntity.notFound().build();
        try {
            Patient patient = patients.get().uri("/patients/1").retrieve().body(Patient.class);
            return ResponseEntity.ok(new Appointment(1, patient));
        } catch (HttpClientErrorException.NotFound ex) {
            return ResponseEntity.notFound().build();
        }
    }

    public record Patient(long id, String name) {}
    public record Appointment(long id, Patient patient) {}
}
