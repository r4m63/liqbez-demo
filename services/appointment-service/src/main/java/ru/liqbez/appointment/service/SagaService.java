package ru.liqbez.appointment.service;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import ru.liqbez.appointment.client.PaymentClient;
import ru.liqbez.appointment.model.SagaResult;

@Service
public class SagaService {
    private final ReservationService reservations;
    private final PaymentClient payments;
    private final ConcurrentHashMap<String, SagaResult> results = new ConcurrentHashMap<>();

    public SagaService(ReservationService reservations, PaymentClient payments) {
        this.reservations = reservations;
        this.payments = payments;
    }

    public SagaResult run(String id, boolean failPayment) {
        if (results.containsKey(id)) return results.get(id);
        reservations.reserve(id);
        try {
            payments.charge(id, failPayment);
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

}
