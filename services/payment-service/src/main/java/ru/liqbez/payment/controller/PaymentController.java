package ru.liqbez.payment.controller;

import org.springframework.web.bind.annotation.*;
import ru.liqbez.payment.model.Payment;
import ru.liqbez.payment.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) { this.service = service; }

    @PostMapping("/{id}")
    public Payment charge(@PathVariable String id, @RequestParam(defaultValue = "false") boolean fail) {
        return service.charge(id, fail);
    }

    @GetMapping("/{id}")
    public Payment find(@PathVariable String id) {
        return service.find(id);
    }
}
