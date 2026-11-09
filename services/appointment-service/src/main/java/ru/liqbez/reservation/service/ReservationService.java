package ru.liqbez.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.liqbez.reservation.model.Reservation;
import ru.liqbez.reservation.repository.ReservationRepository;

@Service
public class ReservationService {
    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) { this.repository = repository; }

    @Transactional
    public Reservation reserve(String id) {
        repository.reserve(id);
        return repository.find(id);
    }

    @Transactional
    public Reservation cancel(String id) {
        repository.cancel(id);
        return find(id);
    }

    public Reservation find(String id) {
        return repository.find(id);
    }
}
