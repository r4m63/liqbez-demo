package ru.liqbez.appointment;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SagaService {
    private final ReservationService reservations;
    private final RestClient payments;
    private final ConcurrentHashMap<String, SagaResult> results = new ConcurrentHashMap<>();

    public SagaService(ReservationService reservations, @Qualifier("serviceRestClientBuilder") RestClient.Builder builder) {
        this.reservations = reservations;
        this.payments = builder.clone().baseUrl("http://payment-service").build();
    }

    public SagaResult run(String id, boolean failPayment) {
        if (results.containsKey(id)) return results.get(id);
        reservations.reserve(id);
        try {
            payments.post().uri("/payments/{id}?fail={fail}", id, failPayment).retrieve().toBodilessEntity();
            SagaResult result = new SagaResult(id, "COMPLETED", "Reservation and payment committed locally");
            results.put(id, result);
            return result;
        } catch (Exception paymentFailure) {
            try {
                reservations.cancel(id);
                SagaResult result = new SagaResult(id, "COMPENSATED", "Payment failed; reservation cancelled");
                results.put(id, result);
                return result;
            } catch (Exception compensationFailure) {
                SagaResult result = new SagaResult(id, "COMPENSATION_FAILED", compensationFailure.getMessage());
                results.put(id, result);
                return result;
            }
        }
    }

    public SagaResult find(String id) { return results.get(id); }

    public record SagaResult(String id, String status, String detail) {}
}
