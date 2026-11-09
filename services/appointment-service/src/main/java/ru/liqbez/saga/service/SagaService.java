package ru.liqbez.saga.service;

import org.springframework.stereotype.Service;
import ru.liqbez.reservation.service.ReservationService;
import ru.liqbez.saga.client.PaymentClient;
import ru.liqbez.saga.model.SagaResult;
import ru.liqbez.saga.repository.SagaRepository;

@Service
public class SagaService {
    private final ReservationService reservations;
    private final PaymentClient payments;
    private final SagaRepository repository;

    public SagaService(ReservationService reservations, PaymentClient payments, SagaRepository repository) {
        this.reservations = reservations;
        this.payments = payments;
        this.repository = repository;
    }

    public SagaResult run(String id, boolean failPayment) {
        SagaResult previous = repository.find(id);
        if (previous != null) return previous;
        reservations.reserve(id);
        try {
            payments.charge(id, failPayment);
            SagaResult result = new SagaResult(id, "COMPLETED", "Reservation and payment committed locally");
            repository.save(result);
            return result;
        } catch (Exception paymentFailure) {
            try {
                reservations.cancel(id);
                SagaResult result = new SagaResult(id, "COMPENSATED", "Payment failed; reservation cancelled");
                repository.save(result);
                return result;
            } catch (Exception compensationFailure) {
                SagaResult result = new SagaResult(id, "COMPENSATION_FAILED", compensationFailure.getMessage());
                repository.save(result);
                return result;
            }
        }
    }

    public SagaResult find(String id) { return repository.find(id); }
}
