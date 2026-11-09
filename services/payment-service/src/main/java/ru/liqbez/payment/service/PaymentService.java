package ru.liqbez.payment.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.liqbez.payment.model.Payment;
import ru.liqbez.payment.repository.PaymentRepository;

@Service
public class PaymentService {
    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Payment charge(String id, boolean fail) {
        if (fail) throw new ResponseStatusException(HttpStatus.CONFLICT, "Demo payment rejection");
        repository.charge(id);
        return new Payment(id, "CHARGED");
    }

    public Payment find(String id) {
        return repository.find(id);
    }
}
