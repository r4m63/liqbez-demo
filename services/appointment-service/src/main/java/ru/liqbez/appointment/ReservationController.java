package ru.liqbez.appointment;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments/reservations")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) { this.service = service; }

    @PostMapping("/{id}")
    public ReservationService.Reservation reserve(@PathVariable String id) { return service.reserve(id); }

    @DeleteMapping("/{id}")
    public ReservationService.Reservation cancel(@PathVariable String id) { return service.cancel(id); }

    @GetMapping("/{id}")
    public ReservationService.Reservation find(@PathVariable String id) { return service.find(id); }
}
