package ru.liqbez.appointment.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.liqbez.appointment.model.Patient;

@Component
public class PatientClient {
    private final RestClient client;

    public PatientClient(@Qualifier("serviceRestClientBuilder") RestClient.Builder builder) {
        this.client = builder.clone().baseUrl("http://patient-service").build();
    }

    public Patient find(long id) {
        return client.get().uri("/patients/{id}", id).retrieve().body(Patient.class);
    }
}
