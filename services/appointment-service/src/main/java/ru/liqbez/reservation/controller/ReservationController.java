package ru.liqbez.reservation.controller;

import org.springframework.web.bind.annotation.*;
import ru.liqbez.reservation.model.Reservation;
import ru.liqbez.reservation.service.ReservationService;

@RestController
@RequestMapping("/appointments/reservations")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) { this.service = service; }

    @PostMapping("/{id}")
    public Reservation reserve(@PathVariable String id) { return service.reserve(id); }

    @DeleteMapping("/{id}")
    public Reservation cancel(@PathVariable String id) { return service.cancel(id); }

    @GetMapping("/{id}")
    public Reservation find(@PathVariable String id) { return service.find(id); }
}
