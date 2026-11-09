package ru.liqbez.transactions;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/transactions/saga")
public class SagaController {
    private final RestClient appointments;
    private final RestClient payments;
    private final ConcurrentHashMap<String, SagaResult> results = new ConcurrentHashMap<>();

    public SagaController(@Qualifier("serviceRestClientBuilder") RestClient.Builder builder) {
        this.appointments = builder.clone().baseUrl("http://appointment-service").build();
        this.payments = builder.clone().baseUrl("http://payment-service").build();
    }

    @PostMapping("/{id}")
    public SagaResult run(@PathVariable String id, @RequestParam(defaultValue = "false") boolean failPayment) {
        if (results.containsKey(id)) return results.get(id);
        appointments.post().uri("/appointments/reservations/{id}", id).retrieve().toBodilessEntity();
        try {
            payments.post().uri("/payments/{id}?fail={fail}", id, failPayment).retrieve().toBodilessEntity();
            SagaResult result = new SagaResult(id, "COMPLETED", "Reservation and payment committed locally");
            results.put(id, result);
            return result;
        } catch (Exception paymentFailure) {
            try {
                appointments.delete().uri("/appointments/reservations/{id}", id).retrieve().toBodilessEntity();
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

    @GetMapping("/{id}")
    public SagaResult find(@PathVariable String id) { return results.get(id); }

    public record SagaResult(String id, String status, String detail) {}
}
