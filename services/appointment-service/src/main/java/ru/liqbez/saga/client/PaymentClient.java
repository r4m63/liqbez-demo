package ru.liqbez.saga.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {
    private final RestClient client;

    public PaymentClient(@Qualifier("serviceRestClientBuilder") RestClient.Builder builder) {
        this.client = builder.clone().baseUrl("http://payment-service").build();
    }

    public void charge(String id, boolean fail) {
        client.post().uri("/payments/{id}?fail={fail}", id, fail).retrieve().toBodilessEntity();
    }
}
